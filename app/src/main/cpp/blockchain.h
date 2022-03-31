#ifndef ICT2105_QUIZ03_2022_SOLUTION_BLOCKCHAIN_H
#define ICT2105_QUIZ03_2022_SOLUTION_BLOCKCHAIN_H

#define HASH_LEN 32

/**
 * The block header.
 */
typedef struct {
    // record the starttime when this block was mined
    uint32_t timestamp;

    // length of the data in the block
    uint32_t dataLength;

    // SHA256 hash of of the data (e.g., transaction message)
    // - this is an array of bytes (uint8_t), i.e., an 8-bit thing each, or a thing with 256 representations
    // - to use it (e.g., print), we normally convert it to a hex String using makeCStringFromBytes
    //   where each element in dataHash will become a 2-char hex digit
    // NOTE: useless knowledge below for the quiz but cool nevertheless :)
    //       - this is super efficient way to detect if contents are tampered with
    //       - in BTC chain this will be the merkle root
    uint8_t dataHash[HASH_LEN];

    // SHA256 hash of the previous header
    // - this is an array of bytes, as described in dataHash above
    // NOTE: useless knowledge below for the quiz but cool nevertheless :)
    //       - this is like a cryptographically secured back ptr in a linked list
    //       - this will connect the whole chain
    //       - super efficient way to detect whether something in the entire chain is tampered with
    uint8_t previousHeaderHash[HASH_LEN];

    // the "magic number" found by mining until the right hash is found
    // NOTE: useless knowledge below for the quiz but cool nevertheless :)
    //       - stands for "number used once"
    //       - different miners in BTC network compete to be the first to find this nonce
    //         who will get to add the block and be rewarded with newly minted BTC
    uint32_t nonce;
} BlockHeader;

BlockHeader addBlockWithPrevPtr(const BlockHeader* prevHeader, const char* data,
                                const uint64_t length, const int difficulty);
void mine(BlockHeader* header, const int difficulty);
void makeCStringFromBytes(const uint8_t * bytes, char* output, const size_t bytesSize);
void makeBytesFromCString(const char* cstring, uint8_t* output, const size_t length);
void fprintHash(FILE* f, const uint8_t* hash);
void logHash(const uint8_t* hash);

#endif //ICT2105_QUIZ03_2022_SOLUTION_BLOCKCHAIN_H