package com.team254.cheezdroid.comm.messages;


import org.json.JSONException;
import org.json.JSONObject;

public class OffWireMessage extends VisionMessage {

    private String mType;
    private String mMessage = "{}";
    private boolean mValid = false;

    public OffWireMessage(String message) {
        try {
            JSONObject reader = new JSONObject(message);
            mType = reader.getString("type");
            mMessage = reader.getString("message");
            mValid = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isValid() {
        return mValid;
    }

    @Override
    public String getType() {
        return mType == null ? "unknown" : mType;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }
}
