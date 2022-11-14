#ifndef int512_helper_h
#define int512_helper_h

#include "caf/all.hpp"
#include "caf/io/all.hpp"
#include "caf/byte.hpp"
#include "int512_serialization.hpp"
#include "is_probable_prime.hpp"
#include "types.hpp"
#include <boost/multiprecision/cpp_int.hpp>
#include <boost/random.hpp>
#include <boost/random/mersenne_twister.hpp>
#include <boost/random/uniform_int_distribution.hpp>
#include <boost/random/random_device.hpp>

namespace int512_helper {

using boost::multiprecision::int512_t;
constexpr auto bytes_in_int512 = 512 / 8;
boost::random::mt19937 gen;

template<size_t num_bytes>
std::array<caf::byte, num_bytes> getRandomizedByteArray() {
    gen.seed(time(nullptr));
    std::array<caf::byte, num_bytes> data{};
    caf::byte* write_ptr = data.data();

    // size
    caf::byte size = static_cast<caf::byte>(1);
    *write_ptr = size;
    ++write_ptr;

    // sign
    caf::byte sign = static_cast<caf::byte>(static_cast<uint8_t>(true));
    *write_ptr = sign;
    ++write_ptr;

    int8_t lim8 = (std::numeric_limits<int8_t>::max)();
    boost::random::uniform_int_distribution<int8_t> dist(1, lim8);
    while(write_ptr != data.end()) {
        int8_t random_byte = dist(gen);
        *write_ptr = static_cast<caf::byte>(random_byte);
        ++write_ptr;
    }
    return data;
}

constexpr auto bytes = bytes_in_int512 + 2;

int512_t genRandomInt512() {
    auto data = getRandomizedByteArray<bytes>();
    caf::byte *read_ptr = data.data() + 2;
    int512_t i512 = 0;
    auto& x = i512.backend();
    uint32_t bytes_to_copy = 64;
    std::copy(read_ptr, read_ptr + bytes_to_copy,
              reinterpret_cast<caf::byte*>(&x));
    return i512;
}

}


#endif /* int512_helper_h */
