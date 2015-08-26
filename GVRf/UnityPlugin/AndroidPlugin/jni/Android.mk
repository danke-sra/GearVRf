LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := GVRFPlugin

ARCH := armeabi-v7a

GVRF_FRAMEWORK_DIR := ../../GVRf/Framework

LOCAL_ARM_NEON := true

LOCAL_C_INCLUDES += \
	$(GVRF_FRAMEWORK_DIR)/jni \
	$(GVRF_FRAMEWORK_DIR)/jni/contrib

LOCAL_CPPFLAGS := -DUNITY_ANDROID
LOCAL_CPPFLAGS += -fpermissive -fexceptions -frtti -std=c++11 -D__GXX_EXPERIMENTAL_CXX0X__ -mhard-float -D_NDK_MATH_NO_SOFTFP=1
LOCAL_LDLIBS := -llog -lEGL -lGLESv3 -ljnigraphics -landroid -lm_hard
LOCAL_LDLIBS += $(GVRF_FRAMEWORK_DIR)/libs/$(ARCH)/libgvrf.so

LOCAL_SRC_FILES := GVRFPluginGLES.cpp

include $(BUILD_SHARED_LIBRARY)
