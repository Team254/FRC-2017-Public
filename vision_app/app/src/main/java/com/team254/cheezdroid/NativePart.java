package com.team254.cheezdroid;

public class NativePart {
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("JNIpart");
    }

    public static final int DISP_MODE_RAW = 0;
    public static final int DISP_MODE_THRESH = 1;
    public static final int DISP_MODE_TARGETS = 2;
    public static final int DISP_MODE_TARGETS_PLUS = 3;

    public static native void processFrame(
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
            TargetsInfo destInfo);

    /**
     * Classes referenced from native code, DO NOT CHANGE ANY NAMING!!!!
     */
    public static class TargetsInfo {
        public static class Target {
            public double centroidX;
            public double centroidY;
            public double width;
            public double height;
        }

        public int numTargets;
        public final Target[] targets;

        public TargetsInfo() {
            targets = new Target[3];
            for (int i = 0; i < targets.length; i++) {
                targets[i] = new Target();
            }
        }
    }
}
