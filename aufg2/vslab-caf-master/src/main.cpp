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

// CAF includes
#include "caf/all.hpp"
#include "caf/io/all.hpp"

// Boost includes
CAF_PUSH_WARNINGS
#ifdef CAF_GCC
#  pragma GCC diagnostic ignored "-Wdeprecated-copy"
#endif

#include <boost/multiprecision/cpp_int.hpp>
#include <boost/random.hpp>

CAF_POP_WARNINGS

// Own includes
#include "int512_serialization.hpp"
#include "is_probable_prime.hpp"
#include "types.hpp"

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

    actor_ostream log(stateful_actor<client_state> *self) const {
        return aout(self) << "[CLIENT " << task << "] ";
    }

};

behavior client(stateful_actor<client_state> *self, caf::group grp, int512_t task) {
    self->set_default_handler(skip);
    self->join(grp);
    self->state.grp = grp;
    self->state.task = task;

    self->state.log(self) << "sending task '" << task << "'" << std::endl;
    self->send(self->state.grp, task_atom_v, task);

    // TODO: Implement me.
    return {
        [=](result_atom, int512_t task, int512_t result, int cpu_time, int rho_cyles) {
            self->state.log(self) << "got result '" << result << "'" << std::endl;
            self->quit();
        },
        [=](idle_request_atom) {
            self->state.log(self) << "got idle request, sending task '" << task << "'" << std::endl;
            self->send(caf::actor_cast<caf::actor>(self->current_sender()), idle_response_atom_v, self->state.task);
        },
    };
}

void run_client(actor_system &sys, const config &cfg) {
    if (auto eg = sys.middleman().remote_group("vslab", cfg.host, cfg.port)) {

        std::vector<caf::actor> v;
        while (true) {
            cout << "Enter a number:" << std::endl;
            int512_t number;
            std::cin >> number;

            auto grp = *eg;
            auto a1 = sys.spawn(client, grp, number);
            v.emplace_back(a1);
            std::this_thread::sleep_for(std::chrono::milliseconds(500));
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

            // TODO: Implement me.
            // - Calculate rho.
            // - Check for new messages in between.
            self->state.task = task;
            self->state.log(self) << "Got task '" << task << "'" << std::endl;
            std::this_thread::sleep_for(std::chrono::seconds(std::rand() % 15));
            int512_t answer = task / 2;
            self->state.log(self) << "Found result: " << answer << std::endl;

            self->send(self, result_atom_v, task, answer, 999, int{0});
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
        [=](result_atom, int512_t task, int512_t result, int cpu_time, int rho_cyles) {
            if (self->id() == self->current_sender()->id()) {
                // Message from myself
                if ( self->state.task == task){
                    // I found the result
                    self->state.log(self) << "Sending result '" << result << "'" << std::endl;
                    self->send(self->state.grp, result_atom_v, task, result, cpu_time, rho_cyles);
                    self->send(self->state.grp, result_atom_v, int512_t{task}, int512_t{result}, int{cpu_time},
                               int{rho_cyles});
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
        int actor_count = 2;
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
