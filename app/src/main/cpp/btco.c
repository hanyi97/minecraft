#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include "sha-256.h"
#include "blockchain.h"

#define TAG "BITCONATIVE"

/**
 * @brief Log difficulty and message
 *
 * @param difficulty network difficulty
 * @param message transaction message
 */
JNIEXPORT void JNICALL
Java_edu_singaporetech_btco_BTCOActivity_logDifficultyMsgNative(JNIEnv *env, jobject thiz,
                                                                jint difficulty,
                                                                jstring message) {

    const char* message_str = (*env)->GetStringUTFChars(env, message, 0);
    __android_log_print(ANDROID_LOG_INFO, TAG, "Difficulty: %d, Message: %s",
                        difficulty, message_str);
}

/**
 * @brief Mine genesis block, log timestamp and return hash
 *
 * @param difficulty network difficulty
 * @return hash result of mining
 */
JNIEXPORT jstring JNICALL
Java_edu_singaporetech_btco_BTCOActivity_mineGenesisBlockNative(JNIEnv *env, jobject thiz,
                                                                jint difficulty) {

    const char data[] = "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks";
    BlockHeader genesisBlock = addBlockWithPrevPtr(NULL, data, sizeof(data), difficulty);

    __android_log_print(ANDROID_LOG_INFO, TAG, "Created block with timestamp=%u nonce=%d",
                        genesisBlock.timestamp, genesisBlock.nonce);

    char hashStr[HASH_LEN * 2 + 1];
    makeCStringFromBytes(genesisBlock.dataHash, hashStr, HASH_LEN);
    return (*env)->NewStringUTF(env, hashStr);

}

/**
 * @brief Mine blocks, log timestamp and return hash of last block
 *
 * @param blocks number of blocks to mine
 * @param difficulty network difficulty
 * @param message transaction message
 *
 * @return hash result of mining of last block
 */
JNIEXPORT jstring JNICALL
Java_edu_singaporetech_btco_BTCOActivity_mineBlocksNative(JNIEnv *env, jobject thiz, jint blocks,
                                                          jint difficulty, jstring message) {

    // Mine genesis block
    const char genesisData[] = "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks";
    const char* message_str = (*env)->GetStringUTFChars(env, message, 0);
    BlockHeader genesisBlock = addBlockWithPrevPtr(NULL, genesisData, sizeof(genesisData), difficulty);

    // Log timestamp of genesis block
    __android_log_print(ANDROID_LOG_INFO, TAG, "Created block with timestamp=%u nonce=%d",
                        genesisBlock.timestamp, genesisBlock.nonce);

    // Do linked list stuff
    BlockHeader* prevBlock = &genesisBlock;
    BlockHeader* lastBlock = NULL;
    for (int i = 1; i < blocks; i++) {
        BlockHeader newBlock = addBlockWithPrevPtr(prevBlock, message_str, sizeof(genesisData)+1, difficulty);
        prevBlock = &newBlock;
        __android_log_print(ANDROID_LOG_INFO, TAG, "Created block with timestamp=%u nonce=%d",
                            newBlock.timestamp, newBlock.nonce);
        if (i == blocks - 1) {
            lastBlock = &newBlock;
        }
    }

    // Convert hash to string
    char hashStr[HASH_LEN * 2 + 1];
    makeCStringFromBytes(lastBlock->dataHash, hashStr, HASH_LEN);
    return (*env)->NewStringUTF(env, hashStr);
}