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

#include <vector>
#include <random>
#include <climits>
#include <algorithm>
#include <functional>

namespace int512_helper {

using boost::multiprecision::int512_t;
using random_bytes_engine = std::independent_bits_engine<
    std::default_random_engine, CHAR_BIT, unsigned char>;


int512_t genRandomInt512() {
    random_bytes_engine rbe;
    std::array<unsigned char, sizeof(size_t)> data {0};
    std::generate(begin(data), end(data), std::ref(rbe));
    return static_cast<int512_t>(data);
}

}


#endif /* int512_helper_h */
