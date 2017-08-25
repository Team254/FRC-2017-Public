package com.team254.frc2017.subsystems;

import edu.wpi.first.wpilibj.DigitalOutput;

import com.team254.frc2017.Constants;
import com.team254.frc2017.loops.Loop;
import com.team254.frc2017.loops.Looper;

/**
 * The LED subsystem consists of the green ring light on the front of the robot used for aiming and communicating
 * information with the drivers (when a gear is picked up, when the robot loses connection) and the blue "range finding"
 * LED strip on the back of the robot used for signaling to the human player. The main things this subsystem has to do
 * is turn each LED on, off, or make it blink.
 * 
 * @see Subsystem.java
 */
public class LED extends Subsystem {
    public static final int kDefaultBlinkCount = 4;
    public static final double kDefaultBlinkDuration = 0.2; // seconds for full cycle
    private static final double kDefaultTotalBlinkDuration = kDefaultBlinkCount * kDefaultBlinkDuration;

    private static LED mInstance = null;

    public static LED getInstance() {
        if (mInstance == null) {
            mInstance = new LED();
        }
        return mInstance;
    }

    // Internal state of the system
    public enum SystemState {
        OFF, FIXED_ON, BLINKING, RANGE_FINDING
    }

    public enum WantedState {
        OFF, FIXED_ON, BLINK, FIND_RANGE
    }

    private SystemState mSystemState = SystemState.OFF;
    private WantedState mWantedState = WantedState.OFF;

    private boolean mIsLEDOn;
    private DigitalOutput mLED;
    private DigitalOutput mRangeLED;
    private boolean mIsBlinking = false;

    private double mBlinkDuration;
    private int mBlinkCount;
    private double mTotalBlinkDuration;

    public LED() {
        mLED = new DigitalOutput(Constants.kGreenLEDId);
        mLED.set(false);

        mRangeLED = new DigitalOutput(Constants.kRangeLEDId);
        setRangeLEDOff();

        // Force a relay change.
        mIsLEDOn = true;
        setLEDOff();

        mBlinkDuration = kDefaultBlinkDuration;
        mBlinkCount = kDefaultBlinkCount;
        mTotalBlinkDuration = kDefaultTotalBlinkDuration;
    }

    private Loop mLoop = new Loop() {
        private double mCurrentStateStartTime;

        @Override
        public void onStart(double timestamp) {
            synchronized (LED.this) {
                mSystemState = SystemState.OFF;
                mWantedState = WantedState.OFF;
                mLED.set(false);
                mIsBlinking = false;
            }

            mCurrentStateStartTime = timestamp;
        }

        @Override
        public void onLoop(double timestamp) {
            synchronized (LED.this) {
                SystemState newState;
                double timeInState = timestamp - mCurrentStateStartTime;
                switch (mSystemState) {
                case OFF:
                    newState = handleOff();
                    break;
                case BLINKING:
                    newState = handleBlinking(timeInState);
                    break;
                case FIXED_ON:
                    newState = handleFixedOn();
                    break;
                case RANGE_FINDING:
                    newState = handleRangeFinding(timeInState);
                    break;
                default:
                    System.out.println("Fell through on LED states!!");
                    newState = SystemState.OFF;
                }
                if (newState != mSystemState) {
                    System.out.println("LED state " + mSystemState + " to " + newState);
                    mSystemState = newState;
                    mCurrentStateStartTime = timestamp;
                }
            }
        }

        @Override
        public void onStop(double timestamp) {
            setLEDOff();
        }
    };

    private SystemState defaultStateTransfer() {
        switch (mWantedState) {
        case OFF:
            return SystemState.OFF;
        case BLINK:
            return SystemState.BLINKING;
        case FIND_RANGE:
            return SystemState.RANGE_FINDING;
        case FIXED_ON:
            return SystemState.FIXED_ON;
        default:
            return SystemState.OFF;
        }
    }

    private synchronized SystemState handleOff() {
        setLEDOff();
        setRangeLEDOff();
        return defaultStateTransfer();
    }

    private synchronized SystemState handleFixedOn() {
        setLEDOn();
        return defaultStateTransfer();
    }

    public synchronized void setRangeBlicking(boolean isBlinking) {
        mIsBlinking = isBlinking;
    }

    private synchronized SystemState handleRangeFinding(double timeInState) {
        // Set main LED on.
        setLEDOn();

        if (mIsBlinking) {
            int cycleNum = (int) (timeInState / (mBlinkDuration / 2.0));
            if ((cycleNum % 2) == 0) {
                setRangeLEDOn();
            } else {
                setRangeLEDOff();
            }
        }
        return defaultStateTransfer();
    }

    private synchronized SystemState handleBlinking(double timeInState) {
        if (timeInState > mTotalBlinkDuration) {
            setLEDOff();
            // Transition to OFF state and clear wanted state.
            mWantedState = WantedState.OFF;
            return SystemState.OFF;
        }

        int cycleNum = (int) (timeInState / (mBlinkDuration / 2.0));
        if ((cycleNum % 2) == 0) {
            setLEDOn();
            setRangeLEDOn();
        } else {
            setLEDOff();
            setRangeLEDOff();
        }
        return SystemState.BLINKING;
    }

    @Override
    public void outputToSmartDashboard() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void zeroSensors() {

    }

    @Override
    public void registerEnabledLoops(Looper enabledLooper) {
        enabledLooper.register(mLoop);
    }

    public synchronized void setWantedState(WantedState state) {
        mWantedState = state;
    }

    public synchronized void setLEDOn() {
        if (!mIsLEDOn) {
            mIsLEDOn = true;
            mLED.set(true);
        }
    }

    public synchronized void setLEDOff() {
        if (mIsLEDOn) {
            mIsLEDOn = false;
            mLED.set(false);
        }
    }

    public synchronized void setRangeLEDOn() {
        mRangeLED.set(true);
    }

    public synchronized void setRangeLEDOff() {
        mRangeLED.set(false);
    }

    public synchronized void configureBlink(int blinkCount, double blinkDuration) {
        mBlinkDuration = blinkDuration;
        mBlinkCount = blinkCount;
        mTotalBlinkDuration = mBlinkCount * mBlinkDuration;
    }
}
