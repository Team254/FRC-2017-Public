package com.team254.cheezdroid.comm;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VisionUpdate {
    protected List<CameraTargetInfo> m_targets;
    protected long m_captured = 0;

    public VisionUpdate(long capturedAtTimestamp) {
        m_captured = capturedAtTimestamp;
        m_targets = new ArrayList<>(3);
    }

    public void addCameraTargetInfo(CameraTargetInfo t) {
        m_targets.add(t);
    }

    public String getSendableJsonString(long timestamp) {
        long captured_ago = (timestamp - m_captured) / 1000000L;  // nanos to millis
        JSONObject j = new JSONObject();
        try {
            j.put("capturedAgoMs", captured_ago);
            JSONArray arr = new JSONArray();
            for (CameraTargetInfo t : m_targets) {
                if (t != null) {
                    arr.put(t.toJson());
                }
            }
            j.put("targets", arr);
        } catch (JSONException e) {
            Log.e("VisionUpdate", "Could not encode JSON");
        }

        return j.toString();
    }
}
