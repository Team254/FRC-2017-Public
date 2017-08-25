package com.team254.frc2017.auto.actions;

import edu.wpi.first.wpilibj.Timer;

import com.team254.frc2017.RobotState;
import com.team254.lib.util.math.RigidTransform2d;

/**
 * Transform's the robot's current pose by a correction constant. Used to correct for error in robot position
 * estimation.
 * 
 * @see Action
 * @see RunOnceAction
 */
public class CorrectPoseAction extends RunOnceAction {
    RigidTransform2d mCorrection;

    public CorrectPoseAction(RigidTransform2d correction) {
        mCorrection = correction;
    }

    @Override
    public void runOnce() {
        RobotState rs = RobotState.getInstance();
        rs.reset(Timer.getFPGATimestamp(), rs.getLatestFieldToVehicle().getValue().transformBy(mCorrection));
    }

}
