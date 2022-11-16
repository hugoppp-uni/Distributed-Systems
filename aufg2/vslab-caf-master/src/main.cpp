// C++ standard library includes
#include <algorithm>
#include <cstdio>
#include <ctime>
#include <iostream>
#include <random>
#include <sstream>
#include <string>
#include <unordered_map>
#include <vector>
#include <stack>
#include <chrono>

// CAF includes
#include "caf/all.hpp"
#include "caf/io/all.hpp"
#include "caf/byte.hpp"

// Boost includes
CAF_PUSH_WARNINGS
#ifdef CAF_GCC
#  pragma GCC diagnostic ignored "-Wdeprecated-copy"
#endif

#include <boost/multiprecision/cpp_int.hpp>
#include <boost/random.hpp>
#include <boost/random/mersenne_twister.hpp>
#include <boost/random/uniform_int_distribution.hpp>
#include <boost/random/random_device.hpp>

CAF_POP_WARNINGS

// Own includes
#include "int512_serialization.hpp"
#include "is_probable_prime.hpp"
#include "types.hpp"
#include "int512_helper.hpp"
#include "pollard_rho.hpp"
//#include "analytics.hpp"

using std::cerr;
using std::cout;
using std::endl;
using std::string;
using std::unordered_map;
using std::vector;

using boost::multiprecision::gcd;
using boost::multiprecision::int512_t;

using namespace caf;

namespace {

struct config : actor_system_config {
    string host = "localhost";
    uint16_t port = 0;
    size_t num_workers = 0;
    string mode;

    config() {
        opt_group{custom_options_, "global"}
            .add(host, "host,H", "server host (ignored in server mode)")
            .add(port, "port,p", "port")
            .add(num_workers, "num-workers,w", "number of workers (in worker mode)")
            .add(mode, "mode,m", "one of 'server', 'worker' or 'client'");
    }
};

// -- SERVER -------------------------------------------------------------------

void run_server(actor_system &sys, const config &cfg) {
    if (auto port = sys.middleman().publish_local_groups(cfg.port))
        cout << "published local groups at port " << *port << '\n';
    else
        cerr << "error: " << caf::to_string(port.error()) << '\n';
    cout << "press any key to exit" << std::endl;
    getc(stdin);
}

// -- CLIENT -------------------------------------------------------------------

// Client state, keep track of factors, time, etc.
struct client_state {
    // The joined group.
    group grp;
    int512_t task;
    long cpu_time;
    uint64_t rho_cycyes;
    std::chrono::steady_clock::time_point begin = std::chrono::steady_clock::now();

    vector<int512_t> prime_factors{};
    std::stack<int512_t> non_prime_factors{};

    actor_ostream log(stateful_actor<client_state> *self) const {
        return aout(self) << "[CLIENT " << task << "] ";
    }

    void add_factor(const int512_t &res) {
        if (is_probable_prime(res)) {
            prime_factors.push_back(res);
        } else {
            non_prime_factors.push(res);
        }
    }

};

void log_factors(stateful_actor<client_state> *self) {
    std::vector<int512_t> factors = self->state.prime_factors;
    std::sort(factors.begin(), factors.end());
    self->state.log(self) << "Factors: ";
    for (int i = 0; i < factors.size(); i++) {
        if (i > 0 && i < factors.size()) aout(self) << " x ";
        aout(self) << factors[i];
    }
    aout(self) << std::endl;
}

behavior client(stateful_actor<client_state> *self, caf::group grp) {
    self->set_default_handler(skip);
    self->join(grp);
    self->state.grp = grp;

    return {
        [=](client_run_atom, int512_t task) {
            self->state.task = task;

            while ((self->state.task % 2) == 0) {
                self->state.prime_factors.emplace_back(2);
                self->state.task = self->state.task / 2;
            }
            if (!self->state.prime_factors.empty()) {
                self->state.log(self) << "Added " << self->state.prime_factors.size()
                                      << " trivial factors, new task: " << self->state.task << std::endl;
            }

            if (self->state.task == 1) {
                self->state.log(self) << "Only trivial factors found" << std::endl;
                log_factors(self);
                return;
            }

            if (is_probable_prime(self->state.task)) {
                self->state.log(self) << "Only trivial factors found, and last one is prime: " << self->state.task
                                      << std::endl;
                self->state.prime_factors.emplace_back(self->state.task);
                log_factors(self);
                return;
            }

            // we need to send a task to the worker (odd non-prime number)
            self->state.log(self) << "Sending first task (odd non-prime number): " << self->state.task << std::endl;
            self->send(self->state.grp, task_atom_v, self->state.task);

        },
        [=](result_atom, int512_t task, int512_t result, long cpu_time, uint64_t rho_cycles) {
            self->state.log(self) << "got result message for task '" << task << "': " << result << std::endl;
            if (task != self->state.task)
                return;

            auto r1 = result;
            auto r2 = task / r1;
            self->state.add_factor(r1);
            self->state.add_factor(r2);
            self->state.cpu_time += cpu_time;
            self->state.rho_cycyes += rho_cycles;

            if (self->state.non_prime_factors.empty()) {
                long wall_clock_time = std::chrono::duration_cast<std::chrono::milliseconds>(
                    std::chrono::steady_clock::now() - self->state.begin).count();
                log_factors(self);
                self->state.log(self) << "done in " << self->state.cpu_time << "ms (CPU), "
                                      << wall_clock_time << "ms (Wall Clock), "
                                      << self->state.rho_cycyes << " rho cycles"
                                      << std::endl;
                self->quit();
                return;
            }

            auto newTask = self->state.non_prime_factors.top();
            self->state.non_prime_factors.pop();
            self->state.task = newTask;
            log_factors(self);
            self->state.log(self) << "sending new, non-prime task '" << newTask << "'" << std::endl;
            self->send(self->state.grp, task_atom_v, int512_t {newTask});

        },
        [=](idle_request_atom) {
            if (self->state.task > 0) {
                self->state.log(self) << "got idle request, sending task '" << self->state.task << "'" << std::endl;
                self->send(caf::actor_cast<caf::actor>(self->current_sender()), idle_response_atom_v, self->state.task);
            }
        },
    };
}

bool valid_input(std::string v) {
    for(int i = 0; i < v.length(); i++) {
        if(!isdigit(v[i])) return false;
    }
    return true;
}

int512_t stoi512(std::string s) {
    int512_t tmp = 0;
    for(int i = 0; i < s.length(); i++) tmp = tmp * 10 + (s[i] - '0');
    return tmp;
}

void run_client(actor_system &sys, const config &cfg) {
    if (auto eg = sys.middleman().remote_group("vslab", cfg.host, cfg.port)) {
        auto grp = *eg;

        while (true) {
            std::cout << "Enter a number:" << std::endl;
            std::string input;
            std::cin >> input;

            if(!valid_input(input)) {
                std::cout << "invalid input" << std::endl;
                std::cin.clear();
            } else {
                int512_t task = stoi512(input);

                scoped_actor self{sys};
                auto a1 = sys.spawn(client, grp);
                self->send(a1, client_run_atom_v, task);

                std::this_thread::sleep_for(std::chrono::milliseconds(500));
            }
        }

    } else {
        cerr << "error: " << caf::to_string(eg.error()) << '\n';
    }
}

// -- WORKER -------------------------------------------------------------------

// State specific to each worker.
struct worker_state {
    // The joined group.
    group grp;
    int512_t task;
    int id;

    actor_ostream log(stateful_actor<worker_state> *self) const {

        auto str = aout(self) << "[WORKER " << id << ", ";
        if (task != 0)
            str << task;
        else
            str << "IDLE";
        return str << "] ";
    }

};

behavior worker(stateful_actor<worker_state> *self, caf::group grp, int id) {
    // Join group and save it to send messages later.
    self->set_default_handler(skip);
    self->join(grp);
    self->state.grp = grp;
    self->state.id = id;
    self->send(self, idle_request_command_atom_v);
    return {
        [=](task_atom, int512_t task) {
            self->state.task = task;
            self->state.log(self) << "Got task '" << task << "'" << std::endl;

            std::chrono::steady_clock::time_point begin = std::chrono::steady_clock::now();

            int512_t answer;
            uint64_t rho_cycles;
            while (answer == 0){
                auto tuple = pollard_rho::pollard_rho(task);
                answer = tuple.first;
                rho_cycles = tuple.second;
                if (answer == 0)
                    self->state.log(self) << "Aborted after " << rho_cycles << "cycles" << std::endl;
            }
            std::chrono::steady_clock::time_point end = std::chrono::steady_clock::now();
            long cpu_time = std::chrono::duration_cast<std::chrono::milliseconds>(end - begin).count();

            self->state.log(self) << "Found result in " << cpu_time << "ms: " << answer
                                  << ", rho cycles: " << rho_cycles << std::endl;
            self->send(self, result_atom_v, int512_t{task}, int512_t{answer}, long{cpu_time}, uint64_t{rho_cycles});
        },
        [=](idle_response_atom, int512_t task) {
            self->state.log(self) << "got idle response: " << task << std::endl;
            if (self->state.task != task) {
                self->send(self, task_atom_v, task);
            }
        },
        [=](idle_request_command_atom) {
            if (self->state.task == 0 && self->mailbox().empty()) {
                int secondsDelay = 10;
                self->state.log(self) << "Sending idle request, scheduled next idle request scheduled in "
                                      << secondsDelay << "s" << std::endl;
                self->send(self->state.grp, idle_request_atom_v);
                self->delayed_send(self, std::chrono::seconds(secondsDelay), idle_request_command_atom_v);
            } else {
                self->state.log(self) << "Scheduled idle request due, but not idle" << std::endl;
            }
        },
        [=](result_atom, int512_t task, int512_t result, long cpu_time, uint64_t rho_cyles) {
            if (self->id() == self->current_sender()->id()) {
                // Message from myself
                if (self->state.task == task) {
                    // I found the result
                    self->state.log(self) << "Sending result '" << result << "'" << std::endl;
                    self->send(self->state.grp, result_atom_v, task, result, cpu_time, rho_cyles);
                    self->send(self->state.grp, result_atom_v, int512_t{task}, int512_t{result}, long{cpu_time},
                               uint64_t{rho_cyles});
                    self->state.task = {};
                    if (self->mailbox().empty()) {
                        self->send(self, idle_request_command_atom_v);
                    }
                }
            } else {
                // Message from somebody else
                if (task == self->state.task) {
                    // Somebody else found the result
                    self->state.log(self) << "Got result from another worker, deleting task" << std::endl;
                    self->send(self, idle_request_command_atom_v);
                    self->state.task = {};
                }
            }
        },
    };
}

void run_worker(actor_system &sys, const config &cfg) {
    if (auto eg = sys.middleman().remote_group("vslab", cfg.host, cfg.port)) {
        auto grp = *eg;
        int actor_count = 16;
        for (int i = 0; i < actor_count; ++i) {
            sys.spawn(worker, grp, i);
        }
        sys.await_all_actors_done();
    } else {
        cerr << "error: " << caf::to_string(eg.error()) << '\n';
    }
    std::this_thread::sleep_for(std::chrono::hours(1));
}

// -- MAIN ---------------------------------------------------------------------

// dispatches to run_* function depending on selected mode
void caf_main(actor_system &sys, const config &cfg) {
    // Check serialization implementation. You can delete this.
    auto check_roundtrip = [&](int512_t a) {
        byte_buffer buf;
        binary_serializer sink{sys, buf};
        assert(sink.apply(a));
        binary_deserializer source{sys, buf};
        int512_t a_copy;
        assert(source.apply(a_copy));
        assert(a == a_copy);
    };
    check_roundtrip(1234912948123);
    check_roundtrip(-124);

    int512_t n = 1;
    for (int512_t i = 2; i <= 50; ++i)
        n *= i;
    check_roundtrip(n);
    n *= -1;
    check_roundtrip(n);

    // Dispatch to function based on mode.
    using map_t = unordered_map<string, void (*)(actor_system &, const config &)>;
    map_t modes{
        {"server", run_server},
        {"worker", run_worker},
        {"client", run_client},
    };
    auto i = modes.find(cfg.mode);
    if (i != modes.end())
        (i->second)(sys, cfg);
    else
        cerr << "*** invalid mode specified" << endl;
}

} // namespace
CAF_MAIN(io::middleman, id_block::vslab)
