//
// Created by mdorn on 10/2/2023.
//

#include <stdint.h>
#include <jni.h>
#include <android/log.h>
#include <android/native_window.h> // requires ndk r5 or newer
#include <android/native_window_jni.h> // requires ndk r5 or newer

#include "logger.h"
#include "renderer.h"

#define LOG_TAG "EglSample"

static ANativeWindow *window = 0;
static ANativeWindow *externalWindow = 0;
static Renderer *renderer = 0;
static Renderer *externalRenderer = 0;

extern "C" JNIEXPORT void JNICALL
Java_com_mihaidornea_opencvapp_shared_utils_views_gles3_GLES3JNILib_nativeOnStart(JNIEnv *jenv,
                                                                                  jobject obj) {
    LOG_INFO("nativeOnStart");
    renderer = new Renderer();
    externalRenderer = new Renderer();
    return;
}

extern "C" JNIEXPORT void JNICALL
Java_com_mihaidornea_opencvapp_shared_utils_views_gles3_GLES3JNILib_nativeOnResume(JNIEnv *jenv,
                                                                                         jobject obj) {
    LOG_INFO("nativeOnResume");
    renderer->start();
    externalRenderer->start();
    return;
}

extern "C" JNIEXPORT void JNICALL
Java_com_mihaidornea_opencvapp_shared_utils_views_gles3_GLES3JNILib_nativeOnPause(JNIEnv *jenv,
                                                                                  jobject obj) {
    LOG_INFO("nativeOnPause");
    renderer->stop();
    externalRenderer->stop();
    return;
}

extern "C" JNIEXPORT void JNICALL
Java_com_mihaidornea_opencvapp_shared_utils_views_gles3_GLES3JNILib_nativeOnStop(JNIEnv *jenv,
                                                                                 jobject obj) {
    LOG_INFO("nativeOnStop");
    delete renderer;
    delete externalRenderer;
    renderer = 0;
    externalRenderer = 0;
    return;
}

extern "C" JNIEXPORT void JNICALL
Java_com_mihaidornea_opencvapp_shared_utils_views_gles3_GLES3JNILib_nativeSetSurface(JNIEnv *jenv,
                                                                                     jobject obj,
                                                                                     jobject surface) {
    if (surface != 0) {
        window = ANativeWindow_fromSurface(jenv, surface);
        LOG_INFO("Got window %p", window);
        renderer->setWindow(window);
    } else {
        LOG_INFO("Releasing window");
        ANativeWindow_release(window);
    }

    return;
}

extern "C" JNIEXPORT void JNICALL
Java_com_mihaidornea_opencvapp_shared_utils_views_gles3_GLES3JNILib_nativeSetExternalSurface(JNIEnv *jenv,
                                                                                     jobject obj,
                                                                                     jobject surface) {
    if (surface != 0) {
        externalWindow = ANativeWindow_fromSurface(jenv, surface);
        LOG_INFO("Got window %p", externalWindow);
        externalRenderer->setWindow(externalWindow);
    } else {
        LOG_INFO("Releasing window");
        ANativeWindow_release(externalWindow);
    }

    return;
}