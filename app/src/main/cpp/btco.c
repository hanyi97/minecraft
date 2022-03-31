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

JNIEXPORT void JNICALL
Java_edu_singaporetech_btco_BTCOActivity_mineGenesisBlockNative(JNIEnv *env, jobject thiz,
                                                                jstring difficulty) {

    const char data[] = "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks";
    addBlockWithPrevPtr(NULL, data, sizeof(data), (int) difficulty);
}


JNIEXPORT void JNICALL
Java_edu_singaporetech_btco_BTCOActivity_mineBlocksNative(JNIEnv *env, jobject thiz, jint blocks,
                                                          jint difficulty, jstring message) {
    const char genesisData[] = "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks";
    const char* message_str = (*env)->GetStringUTFChars(env, message, 0);

    BlockHeader genesisBlock = addBlockWithPrevPtr(NULL, genesisData, sizeof(genesisData), difficulty);
    BlockHeader* prevBlock = &genesisBlock;

    for (int i = 0; i < blocks; i++) {
        BlockHeader newBlock = addBlockWithPrevPtr(prevBlock, message_str, sizeof(genesisData)+1, difficulty);
        prevBlock = &newBlock;
    }
}