#include "image_processor.h"

JNIEXPORT void JNICALL Java_com_team254_cheezdroid_NativePart_processFrame(
    JNIEnv *env,
    jclass cls,
    jint tex1,
    jint tex2,
    jint w,
    jint h,
    jint mode,
    jint h_min,
    jint h_max,
    jint s_min,
    jint s_max,
    jint v_min,
    jint v_max,
    jobject destTargetInfo) {
  processFrame(env, tex1, tex2, w, h, mode, h_min, h_max, s_min, s_max, v_min, v_max, destTargetInfo);
}
