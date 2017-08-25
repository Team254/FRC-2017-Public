package com.team254.cheezdroid.comm;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class CameraTargetInfo {
    protected double m_y;
    protected double m_z;

    // Coordinate frame:
    // +x is out the camera's optical axis
    // +y is to the left of the image
    // +z is to the top of the image
    // We assume the x component of all targets is +1.0 (since this is homogeneous)
    public CameraTargetInfo(double y, double z) {
        m_y = y;
        m_z = z;
    }

    private double doubleize(double value) {
        double leftover = value % 1;
        if (leftover < 1e-7) {
            value += 1e-7;
        }
        return value;
    }

    public double getY() {
        return m_y;
    }

    public double getZ() {
        return m_z;
    }

    public JSONObject toJson() {
        JSONObject j = new JSONObject();
        try {
            j.put("y", doubleize(getY()));
            j.put("z", doubleize(getZ()));
        } catch (JSONException e) {
            Log.e("CameraTargetInfo", "Could not encode Json");
        }
        return j;
    }
}