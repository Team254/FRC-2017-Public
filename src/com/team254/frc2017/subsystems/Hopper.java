package com.team254.frc2017.subsystems;

import edu.wpi.first.wpilibj.Timer;

import com.ctre.CANTalon;

import com.team254.frc2017.Constants;
import com.team254.frc2017.loops.Loop;
import com.team254.frc2017.loops.Looper;
import com.team254.lib.util.Util;
import com.team254.lib.util.drivers.CANTalonFactory;

import java.util.Arrays;

/**
 * The hopper subsystem consists of the 11 floor rollers and 4 shelf rollers that funnel fuel from the robot's storage
 * hopper into the feeder. The rollers are all powered by two 775 Pro motors hooked up two 2 talons. The motors are open
 * loop controlled. The main things this subsystem has to are feed fuel, exhaust fuel, and unjam fuel.
 * 
 * @see Subsystem.java
 */
public class Hopper extends Subsystem {
    private static final double kUnjamInPeriod = .1;
    private static final double kUnjamOutPeriod = .2;
    private static final double kUnjamInPower = .5;
    private static final double kUnjamOutPower = -.5;
    private static final double kFeedPower = 1.0;

    private static Hopper sInstance = null;

    public static Hopper getInstance() {
        if (sInstance == null) {
            sInstance = new Hopper();
        }
        return sInstance;
    }

    private CANTalon mMasterTalon, mSlaveTalon;

    public enum SystemState {
        FEEDING, // feed balls into the feeder subsystem
        UNJAMMING_IN, // used for unjamming fuel
        UNJAMMING_OUT, // used for unjamming fuel
        IDLE, // stop all motors
        EXHAUSTING, // run rollers in reverse
    }

    public enum WantedState {
        IDLE,
        UNJAM,
        EXHAUST,
        FEED,
    }

    private SystemState mSystemState = SystemState.IDLE;
    private WantedState mWantedState = WantedState.IDLE;
    private double mCurrentStateStartTime;
    private boolean mStateChanged;

    private Loop mLoop = new Loop() {
        @Override
        public void onStart(double timestamp) {
            stop();
            synchronized (Hopper.this) {
                mSystemState = SystemState.IDLE;
                mCurrentStateStartTime = timestamp;
                mStateChanged = true;
            }
        }

        @Override
        public void onLoop(double timestamp) {
            synchronized (Hopper.this) {
                SystemState newState;
                switch (mSystemState) {
                case IDLE:
                    newState = handleIdle();
                    break;
                case UNJAMMING_OUT:
                    newState = handleUnjammingOut(timestamp, mCurrentStateStartTime);
                    break;
                case UNJAMMING_IN:
                    newState = handleUnjammingIn(timestamp, mCurrentStateStartTime);
                    break;
                case FEEDING:
                    newState = handleFeeding();
                    break;
                case EXHAUSTING:
                    newState = handleExhaust();
                    break;
                default:
                    newState = SystemState.IDLE;
                }
                if (newState != mSystemState) {
                    System.out.println("Hopper state " + mSystemState + " to " + newState);
                    mSystemState = newState;
                    mCurrentStateStartTime = timestamp;
                    mStateChanged = true;
                } else {
                    mStateChanged = false;
                }
            }
        }

        @Override
        public void onStop(double timestamp) {
            stop();
        }
    };

    private SystemState defaultStateTransfer() {
        switch (mWantedState) {
        case FEED:
            return SystemState.FEEDING;
        case UNJAM:
            return SystemState.UNJAMMING_OUT;
        case EXHAUST:
            return SystemState.EXHAUSTING;
        default:
            return SystemState.IDLE;
        }
    }

    private SystemState handleIdle() {
        setOpenLoop(0);
        return defaultStateTransfer();
    }

    private SystemState handleUnjammingOut(double now, double startStartedAt) {
        setOpenLoop(kUnjamOutPower);
        SystemState newState = SystemState.UNJAMMING_OUT;
        if (now - startStartedAt > kUnjamOutPeriod) {
            newState = SystemState.UNJAMMING_IN;
        }
        switch (mWantedState) {
        case FEED:
            return SystemState.FEEDING;
        case UNJAM:
            return newState;
        case EXHAUST:
            return SystemState.EXHAUSTING;
        default:
            return SystemState.IDLE;
        }
    }

    private SystemState handleUnjammingIn(double now, double startStartedAt) {
        setOpenLoop(kUnjamInPower);
        SystemState newState = SystemState.UNJAMMING_IN;
        if (now - startStartedAt > kUnjamInPeriod) {
            newState = SystemState.UNJAMMING_OUT;
        }
        switch (mWantedState) {
        case FEED:
            return SystemState.FEEDING;
        case UNJAM:
            return newState;
        case EXHAUST:
            return SystemState.EXHAUSTING;
        default:
            return SystemState.IDLE;
        }
    }

    private SystemState handleFeeding() {
        setOpenLoop(kFeedPower);
        switch (mWantedState) {
        case FEED:
            return SystemState.FEEDING;
        case UNJAM:
            return SystemState.UNJAMMING_OUT;
        case EXHAUST:
            return SystemState.EXHAUSTING;
        default:
            return SystemState.IDLE;
        }
    }

    private SystemState handleExhaust() {
        setOpenLoop(-kFeedPower);
        switch (mWantedState) {
        case FEED:
            return SystemState.FEEDING;
        case UNJAM:
            return SystemState.UNJAMMING_OUT;
        case EXHAUST:
            return SystemState.EXHAUSTING;
        default:
            return SystemState.IDLE;
        }
    }

    private Hopper() {
        mMasterTalon = CANTalonFactory.createDefaultTalon(Constants.kHopperMasterId);
        mMasterTalon.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
        mMasterTalon.setVoltageRampRate(Constants.kHopperRampRate);
        mMasterTalon.EnableCurrentLimit(true);
        mMasterTalon.setCurrentLimit(50);
        mMasterTalon.setStatusFrameRateMs(CANTalon.StatusFrameRate.Feedback, 500);
        mMasterTalon.reverseOutput(false);

        mSlaveTalon = CANTalonFactory.createDefaultTalon(Constants.kHopperSlaveId);
        mSlaveTalon.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
        mSlaveTalon.setVoltageRampRate(Constants.kHopperRampRate);
        mSlaveTalon.setStatusFrameRateMs(CANTalon.StatusFrameRate.Feedback, 500);
        mSlaveTalon.EnableCurrentLimit(true);
        mSlaveTalon.setCurrentLimit(50);

        mSlaveTalon.reverseOutput(true);
    }

    public synchronized void setWantedState(WantedState state) {
        mWantedState = state;
    }

    @Override
    public void outputToSmartDashboard() {
    }

    private void setOpenLoop(double openLoop) {
        mMasterTalon.set(-openLoop);
        mSlaveTalon.set(openLoop);
    }

    @Override
    public void stop() {
        setWantedState(WantedState.IDLE);
    }

    @Override
    public void zeroSensors() {

    }

    @Override
    public void registerEnabledLoops(Looper in) {
        in.register(mLoop);
    }

    public boolean checkSystem() {
        System.out.println("Testing HOPPER.--------------------------------------");
        final double kCurrentThres = 0.5;

        mMasterTalon.changeControlMode(CANTalon.TalonControlMode.Voltage);
        mSlaveTalon.changeControlMode(CANTalon.TalonControlMode.Voltage);

        mMasterTalon.set(0.0);
        mSlaveTalon.set(0.0);

        mMasterTalon.set(-6.0f);
        Timer.delay(4.0);
        final double currentMaster = mMasterTalon.getOutputCurrent();
        mMasterTalon.set(0.0);

        Timer.delay(2.0);

        mSlaveTalon.set(6.0f);
        Timer.delay(4.0);
        final double currentSlave = mSlaveTalon.getOutputCurrent();
        mSlaveTalon.set(0.0);

        mMasterTalon.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
        mSlaveTalon.changeControlMode(CANTalon.TalonControlMode.PercentVbus);

        System.out.println("Hopper Master Current: " + currentMaster + " Slave current: " + currentSlave);

        boolean failure = false;

        if (currentMaster < kCurrentThres) {
            failure = true;
            System.out.println("!!!!!!!!!!!!!!!!! Hopper Master Current Low !!!!!!!!!!!!!!!!!");
        }

        if (currentSlave < kCurrentThres) {
            failure = true;
            System.out.println("!!!!!!!!!!!!!!!! Hooper Slave Current Low !!!!!!!!!!!!!!!!!!!");
        }

        if (!Util.allCloseTo(Arrays.asList(currentMaster, currentSlave), currentMaster, 5.0)) {
            failure = true;
            System.out.println("!!!!!!!!!!!!!!!! Hopper Currents Different !!!!!!!!!!!!!!!!!");
        }

        return !failure;
    }
}
