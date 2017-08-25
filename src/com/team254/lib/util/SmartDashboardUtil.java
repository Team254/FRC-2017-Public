package com.team254.lib.util;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Small class filled with static util methods for the SmartDashboard.
 */
public class SmartDashboardUtil {

    public static void deletePersistentKeys() {
        SmartDashboard smartDashboard = new SmartDashboard();
        for (String key : smartDashboard.getKeys()) {
            if (smartDashboard.isPersistent(key)) {
                smartDashboard.delete(key);
            }
        }
    }

    public static void deleteAllKeys() {
        SmartDashboard smartDashboard = new SmartDashboard();
        for (String key : smartDashboard.getKeys()) {
            smartDashboard.delete(key);
        }
    }
}
