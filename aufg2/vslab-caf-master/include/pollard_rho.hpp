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

/* Function to calculate (base^exponent)%modulus */
int512_t modular_pow(int512_t base, int32_t exponent,
                     int512_t modulus)
{
    /* initialize result */
    int512_t result = 1;

    while (exponent > 0)
    {
        /* if y is odd, multiply base with result */
        if (exponent & 1)
            result = (result * base) % modulus;

        /* exponent = exponent/2 */
        exponent = exponent >> 1;

        /* base = base * base */
        base = (base * base) % modulus;
    }
    return result;
}

/* method to return prime divisor for n */
int512_t pollard_rho(int512_t n)
{
    /* no prime divisor for 1 */
    if (n == 1) return n;

    /* even number means one of the divisors is 2 */
    if (n % 2 == 0) return 2;

    /* we will pick from the range [2, N) */
    int512_t x = (int512_helper::genRandomInt512() % (n - 2)) + 2;
    int512_t y = x;

    /* the constant in f(x).
     * Algorithm can be re-run with a different c
     * if it throws failure for a composite. */
    int512_t c = (int512_helper::genRandomInt512() % (n - 1)) + 1;

    /* Initialize candidate divisor (or result) */
    int512_t d = 1;

    /* until the prime factor isn't obtained.
       If n is prime, return n */
    while (d == 1)
    {
        /* Tortoise Move: x(i+1) = f(x(i)) */
        x = (modular_pow(x, 2, n) + c + n) % n;

        /* Hare Move: y(i+1) = f(f(y(i))) */
        y = (modular_pow(y, 2, n) + c + n) % n;
        y = (modular_pow(y, 2, n) + c + n) % n;

        /* check gcd of |x-y| and n */
        d = gcd(abs(x - y), n);

        /* retry if the algorithm fails to find prime factor
         * with chosen x and c */
        if (d == n) return pollard_rho(n);
    }

    return d;
}

}

#endif /* pollard_rho_h */
