#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/features2d.hpp>
#include <vector>
#include <string>
#include <syslog.h>
#include <GLES3/gl3.h>

jbyteArray processImage(JNIEnv *env, const unsigned char *data, jint width, jint height);

jbyteArray matToBytes(JNIEnv *env, cv::Mat &mat);

jbyteArray processExternalImage(JNIEnv *env, const unsigned char *data, int width, int height);


extern "C" JNIEXPORT jstring JNICALL
Java_com_mihaidornea_opencvapp_presentation_MainActivity_stringFromJNI(JNIEnv *env, jobject thiz) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_mihaidornea_opencvapp_presentation_camera_CameraViewModel_processFrame(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray data,
        jint width,
        jint height
) {
    jbyte *byteData = env->GetByteArrayElements(data, nullptr);

    jbyteArray processedMat = processImage(env, reinterpret_cast<unsigned char *>(byteData), width,
                                           height);
    env->ReleaseByteArrayElements(data, byteData, 0);

    return processedMat;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_mihaidornea_opencvapp_presentation_camera_ExternalCameraViewModel_processExternalCameraFrame(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray data,
        jint width,
        jint height
) {
    jbyte *byteData = env->GetByteArrayElements(data, nullptr);

    jbyteArray processedMat = processExternalImage(env, reinterpret_cast<unsigned char *>(byteData),
                                                   width,
                                                   height);
    env->ReleaseByteArrayElements(data, byteData, 0);

    return processedMat;
}

jbyteArray processImage(JNIEnv *env, const unsigned char *data, int width, int height) {
    cv::Mat inputMat(height, width, CV_8UC4, const_cast<unsigned char *>(data));
    cv::cvtColor(inputMat, inputMat, cv::COLOR_RGBA2GRAY);
    cv::rotate(inputMat, inputMat, cv::ROTATE_180);
    cv::cvtColor(inputMat, inputMat, cv::COLOR_GRAY2RGBA);
    jbyteArray result = matToBytes(env, inputMat);
    return result;
}

jbyteArray processExternalImage(JNIEnv *env, const unsigned char *data, int width, int height) {
    cv::Mat inputMat(height, width, CV_8UC4, const_cast<unsigned char *>(data));
    cv::cvtColor(inputMat, inputMat, cv::COLOR_RGBA2GRAY);
    cv::transpose(inputMat, inputMat);
    cv::flip(inputMat, inputMat, 1);
    cv::cvtColor(inputMat, inputMat, cv::COLOR_GRAY2RGBA);
    jbyteArray result = matToBytes(env, inputMat);
    return result;
}

jbyteArray matToBytes(JNIEnv *env, cv::Mat &image) {
    std::vector<unsigned char> bytes;
    if (!image.empty()) {
        bytes.assign(image.data, image.data + image.total() * image.elemSize());
    }
    jbyteArray byteArray = env->NewByteArray(bytes.size());
    if (byteArray != nullptr) {
        env->SetByteArrayRegion(byteArray, 0, bytes.size(),
                                reinterpret_cast<jbyte *>(bytes.data()));
    }
    return byteArray;
}

