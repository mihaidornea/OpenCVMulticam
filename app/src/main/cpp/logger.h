//
// Created by mdorn on 10/2/2023.
//

#ifndef OPENCVAPP_LOGGER_H
#define OPENCVAPP_LOGGER_H

#define LOG_INFO(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOG_ERROR(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#endif //OPENCVAPP_LOGGER_H
