package com.team254.cheezdroid.comm.messages;

import com.team254.cheezdroid.comm.VisionUpdate;

public class TargetUpdateMessage extends VisionMessage {

    VisionUpdate mUpdate;
    long mTimestamp;

    public TargetUpdateMessage(VisionUpdate update, long timestamp) {
        mUpdate = update;
        mTimestamp = timestamp;
    }
    @Override
    public String getType() {
        return "targets";
    }

    @Override
    public String getMessage() {
        return mUpdate.getSendableJsonString(mTimestamp);
    }
}
