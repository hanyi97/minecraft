#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include "sha-256.h"
#include "blockchain.h"

#define TAG "BITCONATIVE"

JNIEXPORT void JNICALL
Java_edu_singaporetech_btco_BTCOActivity_logDifficultyMsgNative(JNIEnv *env, jobject thiz,
                                                                jstring difficulty,
                                                                jstring message) {

    const char* difficulty_str = (*env)->GetStringUTFChars(env, difficulty, 0);
    const char* message_str = (*env)->GetStringUTFChars(env, message, 0);
    __android_log_print(ANDROID_LOG_INFO, TAG, "Difficulty: %s, Message: %s",
                        difficulty_str, message_str);
}

JNIEXPORT jstring JNICALL
Java_edu_singaporetech_btco_BTCOActivity_mineGenesisBlockNative(JNIEnv *env, jobject thiz,
                                                                jint difficulty) {

    const char data[] = "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks";
    BlockHeader genesisBlock = addBlockWithPrevPtr(NULL, data, sizeof(data), difficulty);
    char hashStr[HASH_LEN * 2 + 1];
    makeCStringFromBytes(genesisBlock.dataHash, hashStr, HASH_LEN);
    return (*env)->NewStringUTF(env, hashStr);
}


JNIEXPORT jstring JNICALL
Java_edu_singaporetech_btco_BTCOActivity_mineBlocksNative(JNIEnv *env, jobject thiz, jint blocks,
                                                          jint difficulty, jstring message) {
    const char genesisData[] = "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks";
    const char* message_str = (*env)->GetStringUTFChars(env, message, 0);

    BlockHeader genesisBlock = addBlockWithPrevPtr(NULL, genesisData, sizeof(genesisData), difficulty);
    BlockHeader* prevBlock = &genesisBlock;
    BlockHeader* lastBlock = NULL;

    for (int i = 1; i < blocks; i++) {
        BlockHeader newBlock = addBlockWithPrevPtr(prevBlock, message_str, sizeof(genesisData)+1, difficulty);
        prevBlock = &newBlock;
        if (i == blocks - 1) {
            lastBlock = &newBlock;
        }
    }
    char hashStr[HASH_LEN * 2 + 1];
    makeCStringFromBytes(lastBlock->dataHash, hashStr, HASH_LEN);
    return (*env)->NewStringUTF(env, hashStr);
}