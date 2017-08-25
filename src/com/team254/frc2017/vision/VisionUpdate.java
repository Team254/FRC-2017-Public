package com.team254.frc2017.vision;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * VisionUpdate contains the various attributes outputted by the vision system, namely a list of targets and the
 * timestamp at which it was captured.
 */
public class VisionUpdate {
    protected boolean valid = false;
    protected long capturedAgoMs;
    protected List<TargetInfo> targets;
    protected double capturedAtTimestamp = 0;

    private static long getOptLong(Object n, long defaultValue) {
        if (n == null) {
            return defaultValue;
        }
        return (long) n;
    }

    private static JSONParser parser = new JSONParser();

    private static Optional<Double> parseDouble(JSONObject j, String key) throws ClassCastException {
        Object d = j.get(key);
        if (d == null) {
            return Optional.empty();
        } else {
            return Optional.of((double) d);
        }
    }

    /**
     * Generates a VisionUpdate object given a JSON blob and a timestamp.
     * 
     * @param Capture
     *            timestamp
     * @param JSON
     *            blob with update string, example: { "capturedAgoMs" : 100, "targets": [{"y": 5.4, "z": 5.5}] }
     * @return VisionUpdate object
     */
    //
    public static VisionUpdate generateFromJsonString(double current_time, String updateString) {
        VisionUpdate update = new VisionUpdate();
        try {
            JSONObject j = (JSONObject) parser.parse(updateString);
            long capturedAgoMs = getOptLong(j.get("capturedAgoMs"), 0);
            if (capturedAgoMs == 0) {
                update.valid = false;
                return update;
            }
            update.capturedAgoMs = capturedAgoMs;
            update.capturedAtTimestamp = current_time - capturedAgoMs / 1000.0;
            JSONArray targets = (JSONArray) j.get("targets");
            ArrayList<TargetInfo> targetInfos = new ArrayList<>(targets.size());
            for (Object targetObj : targets) {
                JSONObject target = (JSONObject) targetObj;
                Optional<Double> y = parseDouble(target, "y");
                Optional<Double> z = parseDouble(target, "z");
                if (!(y.isPresent() && z.isPresent())) {
                    update.valid = false;
                    return update;
                }
                targetInfos.add(new TargetInfo(y.get(), z.get()));
            }
            update.targets = targetInfos;
            update.valid = true;
        } catch (ParseException e) {
            System.err.println("Parse error: " + e);
            System.err.println(updateString);
        } catch (ClassCastException e) {
            System.err.println("Data type error: " + e);
            System.err.println(updateString);
        }
        return update;
    }

    public List<TargetInfo> getTargets() {
        return targets;
    }

    public boolean isValid() {
        return valid;
    }

    public long getCapturedAgoMs() {
        return capturedAgoMs;
    }

    public double getCapturedAtTimestamp() {
        return capturedAtTimestamp;
    }

}
