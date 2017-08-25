package com.team254.frc2017.auto.actions;

import com.team254.frc2017.subsystems.Superstructure;

/**
 * Action to extend (or retract) the hopper side panel pistons.
 * 
 * @see Action
 * @see RunOnceAction
 */
public class ActuateHopperAction extends RunOnceAction {

    boolean extend;

    public ActuateHopperAction(boolean extend) {
        this.extend = extend;
    }

    @Override
    public synchronized void runOnce() {
        Superstructure.getInstance().setActuateHopper(extend);
    }
}
