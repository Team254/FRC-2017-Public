package com.team254.frc2017.auto.actions;

import com.team254.frc2017.subsystems.Shooter;

/**
 * Spins up the flywheel to a specified RPM in advance in order to save time later.
 * 
 * @see Action
 * @see RunOnceAction
 */
public class SetFlywheelRPMAction extends RunOnceAction {

    double rpm;

    public SetFlywheelRPMAction(double s) {
        rpm = s;
    }

    @Override
    public synchronized void runOnce() {
        Shooter.getInstance().setSpinUp(rpm);
    }
}
