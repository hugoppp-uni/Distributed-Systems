/**
 * This file is part of the research library Procedural Augmented Reality of the computer graphics
 * group led by Philipp Jenke at the University of Applied Sciences Hamburg.
 */

#ifndef pollard_rho_h
#define pollard_rho_h

#include "boost/multiprecision/cpp_int.hpp"
#include "boost/multiprecision/cpp_int.hpp"
#include "int512_serialization.hpp"
#include "is_probable_prime.hpp"
#include "types.hpp"
#include "int512_helper.hpp"


namespace pollard_rho
{

using boost::multiprecision::int512_t;

std::pair<int512_t, int64_t> pollard_rho(int512_t n) {
    int512_t a, x, y, p, d;
    uint64_t rho_cycles = 0;

    a = (int512_helper::genRandomInt512() % n) + 1;
    x = (int512_helper::genRandomInt512() % n) + 1;
    y = x;
    p = 1;
    d = 0;

    do {
        x = ((x * x) + a) % n;
        y = ((y * y) + a) % n;
        y = ((y * y) + a) % n;
        d = (abs(y-x)) % n;
        p = gcd(d, n);
        rho_cycles++;

    } while(p == 1);
    if(p != n) return {p, rho_cycles};
}

}

#endif /* pollard_rho_h */
