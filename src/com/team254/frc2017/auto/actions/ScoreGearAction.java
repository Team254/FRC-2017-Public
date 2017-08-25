package com.team254.frc2017.auto.actions;

import com.team254.frc2017.subsystems.MotorGearGrabber;
import com.team254.frc2017.subsystems.MotorGearGrabber.WantedState;

/**
 * Action for scoring a gear
 * 
 * @see Action
 * @see RunOnceAction
 */
public class ScoreGearAction extends RunOnceAction {

    @Override
    public void runOnce() {
        MotorGearGrabber.getInstance().setWantedState(WantedState.SCORE);
    }
}
