#ifndef SHA_256_H
#define SHA_256_H

#include <stdio.h>
#include <stdlib.h>

/**
 * Calculate the SHA256 hash from an arbitrary array of bytes.
 * - there is no need to understand the details for this quiz (or for any other purposes unless
 *   you are a cryptography researcher/developer)
 * - you just need to know that given any series of bytes in the input, this func will give you back
 *   a unique fixed length 32-byte hash (32*8-bit bytes gives you 256 yo)
 * @param hash  - ptr to the output 32-byte array of 8-bit bytes
 * @param input - ptr to the input bytes
 * @param len   - number of bytes to read
 */
void calc_sha_256(uint8_t hash[32], const void *input, size_t len);

#endif