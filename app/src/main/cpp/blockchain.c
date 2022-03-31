/*
 * A collection of C funcs that demonstrates a simplified model of the blockchain concept behind BTC.
 * NOTE that you should not change anything in here.
 *
 * Au†hor: chek adapted this from reading
 *         https://github.com/justinmeiners/tiny-blockchain
 *         https://hackernoon.com/learn-blockchains-by-building-one-117428612f46
 * Last updated: 25 Mar 2022
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <time.h>
#include <memory.h>
#include <assert.h>
#include <android/log.h>

#include "sha-256.h"
#include "blockchain.h"

#define LINE_MAX 4096 // max chars for console input

static const char* TAG = "MIN3NATIV3";

/**
 * Make a C-style string in the 2-char hex format, from an array of bytes.
 * - Print each byte stored in the bytes array into a hex number
 *   occupying 2 char slots in the output C-style string
 * - e.g., a bytes input of 10 items will be converted into 20-char C-style string output
 * - NOTE that addBlockWithPrevPtr(...) has examples of how to use this func
 * @param bytes  - the C array of 8-bit bytes
 * @param output - the C-style string output, assumed that you
 *                 have initialized this to 2*bytesSize+1 already
 *                 (the +1 is to place the C String terminator)
 * @param bytesSize - the length of the bytes array
 */
void makeCStringFromBytes(const uint8_t* bytes, char* output, const size_t bytesSize) {
    // add C string terminator in the last slot
    output[2*bytesSize] = '\0';
    for (int i=0; i<bytesSize; i++)
        // "print" byte hex values into buffer
        sprintf(output+i*2, "%02x", bytes[i]);
}

/**
 * Convert a char (in a human-readable string) to a hex char.
 * @param c is the char
 * @return the hex char
 */
unsigned char hexChar(char c)
{
    if ('0' <= c && c <= '9') return (unsigned char)(c - '0');
    if ('A' <= c && c <= 'F') return (unsigned char)(c - 'A' + 10);
    if ('a' <= c && c <= 'f') return (unsigned char)(c - 'a' + 10);
    return 0xFF;
}

/**
 * Convert a C-style string to a bytes array.
 * [code adapted from
 * https://stackoverflow.com/questions/18267803/how-to-correctly-convert-a-hex-string-to-byte-array-in-c]
 * @param cstring - the C-styled string of 2-char hex numbers
 * @param output - C array of 8-bit bytes
 * @param length - length of the bytes array
 */
void makeBytesFromCString(const char* cstring, uint8_t* output, const size_t length) {
    int result;
    if (!cstring || !output || length <= 0) return;

    size_t i = length;
    for (result = 0; *cstring; ++result) {
        unsigned char msn = hexChar(*cstring++);
        if (msn == 0xFF) return;
        unsigned char lsn = hexChar(*cstring++);
        if (lsn == 0xFF) return;
        unsigned char bin = (msn << 4) + lsn;

        if (i-- <= 0) return;
        *output++ = bin;
    }
}

/**
 * Print the hash in 2-char hex format.
 * - this is a logging util func mainly for debugging
 * @param f is the destination to print into
 * @param hash is the ptr to the hash value in mem
 */
void fprintHash(FILE* f, const uint8_t* hash) {
    fprintf(f, "0x");
    for (int i = 0; i < HASH_LEN; ++i)
        fprintf(f, "%02x", hash[i]);
}

/**
 * Debug the hash in Android console, prints each hex digit on a newline.
 * - this is a logging util func mainly for debugging
 * @param hash is the ptr to the hash value in mem
 */
void logHash(const uint8_t* hash) {
    for (int i = 0; i < HASH_LEN; ++i)
        __android_log_print(
                ANDROID_LOG_INFO, TAG,
                "logHash %02x",
                hash[i]
                );
}

/**
 * Mine a block to add to the chain. The gist of the algo is:
 * - repeatedly get a SHA256 hash from the header (by changing the nonce and timestamp)
 *   until hash is < targetHash (or more leading zeros than targetHash)
 * - network difficulty is governed by how many possible combinations you allow by specifying a
 *   targetHash as the upper bound
 * NOTE that this is a mining task that may take a long time.
 *      - the higher the difficulty the (exponentially) longer it becomes.
 * @param header the header of the block initialized somewhere else.
 * @param difficulty of the mining task, expressed as a number from 1..10
 */
void mine(BlockHeader* header, const int difficulty) {
    // change the difficulty by manipulating the leading zeros of the targetHash
    uint8_t targetHash[HASH_LEN]; // create targetHash array of bytes
    memset(targetHash, 0, sizeof(targetHash)); // init to all 0s
    switch (difficulty) {
        case 2:
            targetHash[1] = 0x0F;
            break;
        case 3:
            targetHash[1] = 0x01;
            break;
        case 4:
            targetHash[2] = 0xFF;
            break;
        case 5:
            targetHash[2] = 0x0F;
            break;
        case 6:
            targetHash[2] = 0x01;
            break;
        case 7:
            targetHash[3] = 0xFF;
            break;
        case 8:
            targetHash[3] = 0x0F;
            break;
        case 9:
            targetHash[3] = 0x01;
            break;
        case 10:
            targetHash[4] = 0xFF;
            break;
        default:
            targetHash[1] = 0xFF;
    }

    // Perform mining
    while (1) {
        // record the starttime of this mining round that may potentially get the correct hash
        header->timestamp = (uint64_t)time(NULL);

        // iteratively find the nonce that results in a header hash that is < the targetHash hash
        uint8_t currHeaderHash[HASH_LEN];
        for (uint32_t i = 0; i < UINT32_MAX; ++i)
        {
            // record the nonce that may potentially get a valid hash
            header->nonce = i;

            // hash the header that has the new nonce
            calc_sha_256(currHeaderHash, header, sizeof(BlockHeader));

            // return when the correct hash found
            if (memcmp(currHeaderHash, targetHash, sizeof(currHeaderHash)) < 0)
                return;
        }
        // when all uint32 exhausted without a valid hash, go for the next round with to find the
        // right time + nonce combo that may result in a valid hash
    }

    // this code is never reached
    assert(0);
}

/**
 * Construct all the details needed for a new block with ptr to the actual prevHeader header.
 * NOTE that this func includes mining that may take a LONG TIME.
 * @param prevHeader ptr to the prevHeader header
 * @param data ptr to the data that this block will be representing (null will mean Genesis)
 * @param length of the data to read from the ptr
 * @return the constructed header for the new block
 */
BlockHeader addBlockWithPrevPtr(
        const BlockHeader* prevHeader,
        const char* data,
        const uint64_t length,
        const int difficulty
        ) {
    BlockHeader header;
    header.dataLength = length;
    char logStr[50];

    // obtain the hash of the prevHeader header and store it in this header
    if (prevHeader) {
        calc_sha_256(header.previousHeaderHash, prevHeader, sizeof(BlockHeader));
        strcpy(logStr, "addBlockWithPrevPtr:BLOCK:%s:%s");
    }

    // no prevHeader (null) means that this is the Genesis (first) block
    else {
        memset(header.previousHeaderHash, 0, sizeof(header.previousHeaderHash));
        strcpy(logStr, "addBlockWithPrevPtr:GENESIS:%s:%s");
    }

    // obtain the hash of the data and store it in this header
    calc_sha_256(header.dataHash, data, length);

    // DEBUG log
    char dataHashStr[HASH_LEN * 2 + 1];
    char prevHashStr[HASH_LEN * 2 + 1];
    makeCStringFromBytes(header.dataHash, dataHashStr, HASH_LEN);
    makeCStringFromBytes(header.previousHeaderHash, prevHashStr, HASH_LEN);
    __android_log_print(
            ANDROID_LOG_INFO, TAG,
            logStr,
            dataHashStr,
            prevHashStr
    );

    // perform the mining operation
    // NOTE that this may take a LONG TIME
    mine(&header, difficulty);

    // return the constructed header
    return header;
}

/**
 * This is a function to let you test the functions independently as a compiled C executable.
 * NOTE that this function has not been tested for ages and so use it at your own discretion.
 *      however, there may be hints of C syntax for calling the various functions :)
 * NOTE if you are running this from command line you need to remove the android dependency
 *      `#include <android/log.h>`
 * @param argc - count of command line arguments
 * @param argv - array containing the command line arguments
 * @return standard C program exit code
 */
int main(int argc, const char* argv[]) {
    // set a fixed network difficulty
    int difficulty = 3;

    // create/open bin file to store the BTC blockchain
    FILE* outFile = fopen("btchain.bin", "wb");

    // create genesisHdr block
    char genesisData[] = "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks";
    BlockHeader genesisHdr = addBlockWithPrevPtr(NULL, genesisData,
                                                 sizeof(genesisData), difficulty);

    // loop to generate subsequent blocks from text read from the console input
    int blockNo = 0;
    BlockHeader prevHdr = genesisHdr;
    while (!feof(stdin))
    {
        // obtain hash of the prevHdr (solved) currHdr
        uint8_t prevHdrHash[HASH_LEN];
        calc_sha_256(prevHdrHash, &prevHdr, sizeof(BlockHeader));

        // display the prevHdr nonce and the prev hashed currHdr
        printf("adding block on the chain with nonce: %i and hash: ", prevHdr.nonce);
        fprintHash(stdout, prevHdrHash);
        printf("\n");

        // persist prevHdr (solved) currHdr to blockchain file
        fwrite(&prevHdr, sizeof(BlockHeader), 1, outFile);

        // read data from the console into a buffer
        char consoleInput[LINE_MAX];
        fgets(consoleInput, LINE_MAX, stdin);

        // create the current block with the data from the console
        printf("creating block %i: ", blockNo);
        printf("%s\n", consoleInput);
        uint64_t size = strnlen(consoleInput, LINE_MAX) + 1; // NOTE the +1
        BlockHeader currHdr = addBlockWithPrevPtr(&prevHdr, consoleInput, size, difficulty);

        prevHdr = currHdr;
        ++blockNo;
    }

    fclose(outFile);
    return 1;
}