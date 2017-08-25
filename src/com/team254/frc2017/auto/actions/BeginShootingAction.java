package com.team254.frc2017.auto.actions;

import com.team254.frc2017.subsystems.Drive;
import com.team254.frc2017.subsystems.Intake;
import com.team254.frc2017.subsystems.Superstructure;

/**
 * Action to begin shooting.
 * 
 * @see Action
 * @see RunOnceAction
 */
public class BeginShootingAction extends RunOnceAction implements Action {

    @Override
    public void runOnce() {
        Drive.getInstance().setWantAimToGoal();
        Superstructure.getInstance().setWantedState(Superstructure.WantedState.SHOOT);
        Intake.getInstance().setOn(); // maybe intake a few missed balls if we're lucky
    }

}
