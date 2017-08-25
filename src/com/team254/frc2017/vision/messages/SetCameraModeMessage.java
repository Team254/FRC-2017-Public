package com.team254.frc2017.vision.messages;

/**
 * A Message that contains and can set the state of the camera and intake systems.
 */
public class SetCameraModeMessage extends VisionMessage {

    private static final String K_VISION_MODE = "vision";
    private static final String K_INTAKE_MODE = "intake";

    private String mMessage = K_VISION_MODE;

    private SetCameraModeMessage(String message) {
        mMessage = message;
    }

    public static SetCameraModeMessage getVisionModeMessage() {
        return new SetCameraModeMessage(K_VISION_MODE);
    }

    public static SetCameraModeMessage getIntakeModeMessage() {
        return new SetCameraModeMessage(K_INTAKE_MODE);
    }

    @Override
    public String getType() {
        return "camera_mode";
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
