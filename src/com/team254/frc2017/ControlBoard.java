package com.team254.frc2017;

import edu.wpi.first.wpilibj.Joystick;

/**
 * Contains the button mappings for the competition control board. Like the drive code, one instance of the ControlBoard
 * object is created upon startup, then other methods request the singleton ControlBoard instance. Implements the
 * ControlBoardInterface.
 * 
 * @see ControlBoardInterface.java
 */
public class ControlBoard implements ControlBoardInterface {
    private static ControlBoardInterface mInstance = null;

    private static final boolean kUseGamepad = false;

    public static ControlBoardInterface getInstance() {
        if (mInstance == null) {
            if (kUseGamepad) {
                mInstance = new GamepadControlBoard();
            } else {
                mInstance = new ControlBoard();
            }
        }
        return mInstance;
    }

    private final Joystick mThrottleStick;
    private final Joystick mTurnStick;
    private final Joystick mButtonBoard;

    protected ControlBoard() {
        mThrottleStick = new Joystick(0);
        mTurnStick = new Joystick(1);
        mButtonBoard = new Joystick(2);
    }

    // DRIVER CONTROLS
    @Override
    public double getThrottle() {
        return -mThrottleStick.getRawAxis(0);
    }

    @Override
    public double getTurn() {
        return -mTurnStick.getY();
    }

    @Override
    public boolean getQuickTurn() {
        return mTurnStick.getRawButton(1);
    }

    @Override
    public boolean getLowGear() {
        return mThrottleStick.getRawButton(2);
    }

    @Override
    public boolean getAimButton() {
        return mTurnStick.getRawButton(2);
    }

    public boolean getDriveAimButton() {
        return mThrottleStick.getRawButton(1);
    }

    // OPERATOR CONTROLS
    @Override
    public boolean getFeedButton() {
        return mButtonBoard.getRawAxis(1) < -0.1;
    }

    @Override
    public boolean getIntakeButton() {
        return mButtonBoard.getRawAxis(2) < -0.1;
    }

    @Override
    public boolean getShooterOpenLoopButton() {
        return false;
    }

    @Override
    public boolean getExhaustButton() {
        return mButtonBoard.getRawAxis(0) < -0.1;
    }

    @Override
    public boolean getUnjamButton() {
        return mButtonBoard.getRawButton(4);
    }

    @Override
    public boolean getShooterClosedLoopButton() {
        return mButtonBoard.getRawButton(8);
    }

    @Override
    public boolean getFlywheelSwitch() {
        return mButtonBoard.getRawAxis(3) < -0.1;
    }

    @Override
    public boolean getHangButton() {
        return mButtonBoard.getRawButton(10);
    }

    @Override
    public boolean getGrabGearButton() {
        return mButtonBoard.getRawButton(2);
    }

    @Override
    public boolean getScoreGearButton() {
        return mButtonBoard.getRawButton(1);
    }

    @Override
    public boolean getActuateHopperButton() {
        return mButtonBoard.getRawButton(3);
    }

    @Override
    public boolean getBlinkLEDButton() {
        return mButtonBoard.getRawButton(9);
    }

    @Override
    public boolean getRangeFinderButton() {
        return mButtonBoard.getRawButton(7);
    }

    @Override
    public boolean getWantGearDriveLimit() {
        return mButtonBoard.getRawButton(12);
    }
}
