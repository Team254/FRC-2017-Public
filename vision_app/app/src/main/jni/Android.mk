LOCAL_PATH := $(call my-dir)

# add OpenCV
include $(CLEAR_VARS)
include $(LOCAL_PATH)/OpenCV.mk

LOCAL_MODULE    := JNIpart
LOCAL_SRC_FILES := jni.c image_processor.cpp
LOCAL_LDLIBS    += -llog -lGLESv2 -lEGL -ldl
LOCAL_CPPFLAGS  += -O3 -std=c++11

include $(BUILD_SHARED_LIBRARY)
