package com.team254.frc2017.auto.actions;

import com.team254.frc2017.subsystems.Superstructure;

/**
 * Action to make the robot stop shooting
 * 
 * @see Action
 * @see RunOnceAction
 */
public class EndShootingAction extends RunOnceAction implements Action {

    @Override
    public void runOnce() {
        Superstructure.getInstance().setWantedState(Superstructure.WantedState.IDLE);
    }

}
