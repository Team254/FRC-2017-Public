package com.team254.frc2017.auto.actions;

/**
 * Action Interface, an interface that describes an iterative action. It is run by an autonomous action, called by the
 * method runAction in AutoModeBase (or more commonly in autonomous modes that extend AutoModeBase)
 *
 * @see com.team254.frc2017.auto.AutoModeBase#runAction
 */
public interface Action {

    /**
     * Returns whether or not the code has finished execution. When implementing this interface, this method is used by
     * the runAction method every cycle to know when to stop running the action
     * 
     * @return boolean
     */
    public abstract boolean isFinished();

    /**
     * Called by runAction in AutoModeBase iteratively until isFinished returns true. Iterative logic lives in this
     * method
     */
    public abstract void update();

    /**
     * Run code once when the action finishes, usually for clean up
     */
    public abstract void done();

    /**
     * Run code once when the action is started, for set up
     */
    public abstract void start();
}
