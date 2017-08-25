package com.team254.frc2017.loops;

import com.team254.frc2017.GoalTracker;
import com.team254.frc2017.RobotState;
import com.team254.frc2017.vision.VisionUpdate;
import com.team254.frc2017.vision.VisionUpdateReceiver;

/**
 * This function adds vision updates (from the Nexus smartphone) to a list in RobotState. This helps keep track of goals
 * detected by the vision system. The code to determine the best goal to shoot at and prune old Goal tracks is in
 * GoalTracker.java
 * 
 * @see GoalTracker.java
 */
public class VisionProcessor implements Loop, VisionUpdateReceiver {
    static VisionProcessor instance_ = new VisionProcessor();
    VisionUpdate update_ = null;
    RobotState robot_state_ = RobotState.getInstance();

    public static VisionProcessor getInstance() {
        return instance_;
    }

    VisionProcessor() {
    }

    @Override
    public void onStart(double timestamp) {
    }

    @Override
    public void onLoop(double timestamp) {
        VisionUpdate update;
        synchronized (this) {
            if (update_ == null) {
                return;
            }
            update = update_;
            update_ = null;
        }
        robot_state_.addVisionUpdate(update.getCapturedAtTimestamp(), update.getTargets());
    }

    @Override
    public void onStop(double timestamp) {
        // no-op
    }

    @Override
    public synchronized void gotUpdate(VisionUpdate update) {
        update_ = update;
    }

}
