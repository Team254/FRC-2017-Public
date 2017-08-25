#pragma once

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

  void processFrame(JNIEnv* env,
                    int tex1,
                    int tex2,
                    int w,
                    int h,
                    int mode,
                    int h_min,
                    int h_max,
                    int s_min,
                    int s_max,
                    int v_min,
                    int v_max,
                    jobject destTargetInfo);

#ifdef __cplusplus
}
#endif
