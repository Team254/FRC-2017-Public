package com.team254.frc2017.subsystems;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import com.ctre.CANTalon;

import com.team254.frc2017.Constants;
import com.team254.frc2017.loops.Loop;
import com.team254.frc2017.loops.Looper;
import com.team254.lib.util.drivers.CANTalonFactory;
import com.team254.lib.util.drivers.MB1043;

/**
 * The gear grabber subsystem consists of one BAG motor used to intake and exhaust gears and one pancake piston used to
 * pivot the entire subsystem from a floor pickup position to a scoring position. The motor is driven in open loop.
 * Since the subsystem lacks any encoders, it detects when a gear has been acquired by checking whether current is above
 * a threshold value. The main things this subsystem has to are intake gears, score gears, and clear balls (run motor in
 * reverse while the grabber is down to push balls away).
 * 
 * @see Subsystem.java
 */
public class MotorGearGrabber extends Subsystem {

    public static boolean kWristDown = false;
    public static boolean kWristUp = !kWristDown;
    public static double kBallClearSetpoint = 8;
    public static double kScoreGearSetpoint = 12;
    public static double kIntakeGearSetpoint = -12;
    public static double kTransitionDelay = 0.5;
    public static double kExhaustDelay = 0.1;
    public static double kIntakeThreshold = 15;
    public static double kThresholdTime = 0.15;

    private static MotorGearGrabber mInstance;

    public static MotorGearGrabber getInstance() {
        if (mInstance == null) {
            mInstance = new MotorGearGrabber();
        }
        return mInstance;
    }

    public enum WantedState {
        IDLE,
        ACQUIRE,
        SCORE,
        CLEAR_BALLS
    }

    private enum SystemState {
        BALL_CLEARING, // grabber down, motors in reverse
        INTAKE, // grabber down, motor intaking
        STOWING, // transition state between INTAKE and STOWED where the motor
                 // intakes so the gear doesn't fly out as the subsystem pivots up
        STOWED, // grabber up, motor stopped
        EXHAUST, // grabber down, motor in reverse
    }

    private final Solenoid mWristSolenoid;
    private final CANTalon mMasterTalon;

    private WantedState mWantedState;
    private SystemState mSystemState;
    private double mThresholdStart;

    private MotorGearGrabber() {
        mWristSolenoid = Constants.makeSolenoidForId(Constants.kGearWristSolenoid);
        mMasterTalon = CANTalonFactory.createDefaultTalon(Constants.kGearGrabberId);
        mMasterTalon.setStatusFrameRateMs(CANTalon.StatusFrameRate.General, 15);
        mMasterTalon.changeControlMode(CANTalon.TalonControlMode.Voltage);
    }

    @Override
    public void outputToSmartDashboard() {
        SmartDashboard.putNumber("Gear Grabber Current", mMasterTalon.getOutputCurrent());
    }

    @Override
    public void stop() {
        setWantedState(WantedState.IDLE);
    }

    @Override
    public void zeroSensors() {

    }

    @Override
    public void registerEnabledLoops(Looper enabledLooper) {
        Loop loop = new Loop() {
            private double mCurrentStateStartTime;

            @Override
            public void onStart(double timestamp) {
                synchronized (MotorGearGrabber.this) {
                    mSystemState = SystemState.STOWING;
                    mWantedState = WantedState.IDLE;
                }
                mCurrentStateStartTime = Timer.getFPGATimestamp();
            }

            @Override
            public void onLoop(double timestamp) {

                synchronized (MotorGearGrabber.this) {
                    SystemState newState = mSystemState;
                    double timeInState = Timer.getFPGATimestamp() - mCurrentStateStartTime;
                    switch (mSystemState) {
                    case BALL_CLEARING:
                        newState = handleBallClearing();
                        break;
                    case INTAKE:
                        newState = handleIntake(timeInState);
                        break;
                    case STOWING:
                        newState = handleStowing(timeInState);
                        break;
                    case STOWED:
                        newState = handleStowed(timeInState);
                        break;
                    case EXHAUST:
                        newState = handleExhaust(timeInState);
                        break;
                    default:
                        System.out.println("Unexpected gear grabber system state: " + mSystemState);
                        newState = mSystemState;
                        break;
                    }

                    if (newState != mSystemState) {
                        System.out.println(timestamp + ": Changed state: " + mSystemState + " -> " + newState);
                        mSystemState = newState;
                        mCurrentStateStartTime = Timer.getFPGATimestamp();
                    }
                }

            }

            @Override
            public void onStop(double timestamp) {
                mWantedState = WantedState.IDLE;
                mSystemState = SystemState.STOWED;
                // Set the states to what the robot falls into when disabled.
                stop();
            }
        };
        enabledLooper.register(loop);
    }

    private SystemState handleBallClearing() {
        setWristDown();
        mMasterTalon.set(kBallClearSetpoint);

        switch (mWantedState) {
        case ACQUIRE:
            return SystemState.INTAKE;
        case CLEAR_BALLS:
            return SystemState.BALL_CLEARING;
        default:
            return SystemState.STOWED;
        }
    }

    private SystemState handleIntake(double timeInState) {
        switch (mWantedState) {
        case CLEAR_BALLS:
            return SystemState.BALL_CLEARING;
        case IDLE:
            return SystemState.STOWING;
        // Fall through intended.
        default:
            setWristDown();
            mMasterTalon.set(kIntakeGearSetpoint);
            // check if the current has been above a threshold value for enough time.
            // If so, blink the LED to let the drivers know we have a gear
            if (mMasterTalon.getOutputCurrent() > kIntakeThreshold) {
                if (timeInState - mThresholdStart > kThresholdTime) {
                    LED.getInstance().setWantedState(LED.WantedState.BLINK);
                } else {
                    if (mThresholdStart == Double.POSITIVE_INFINITY) {
                        mThresholdStart = timeInState;
                    }
                }
            } else {
                mThresholdStart = Double.POSITIVE_INFINITY;
                LED.getInstance().setWantedState(LED.WantedState.OFF);
            }
            return SystemState.INTAKE;
        }
    }

    private SystemState handleExhaust(double timeInState) {
        setWristDown();

        // let the grabber lower a little bit before spitting the gear out.
        // leads to more consistent scoring
        if (timeInState > kExhaustDelay) {
            mMasterTalon.set(kScoreGearSetpoint);
        } else {
            mMasterTalon.set(0);
        }

        // Trap system in exhaust state for a little while.
        if (timeInState < kTransitionDelay) {
            return SystemState.EXHAUST;
        }

        switch (mWantedState) {
        case SCORE:
            return SystemState.EXHAUST;
        case ACQUIRE:
            return SystemState.INTAKE;
        default:
            return SystemState.STOWED;
        }
    }

    public SystemState handleStowing(double timeInState) {
        setWristUp();
        // keep sucking the gear in to make sure it doesn't fly out as the
        // grabber pivots up
        mMasterTalon.set(kIntakeGearSetpoint);
        if (timeInState > kTransitionDelay) {
            return SystemState.STOWED;
        }
        return SystemState.STOWING;
    }

    private SystemState handleStowed(double timeInState) {
        switch (mWantedState) {
        case SCORE:
            return SystemState.EXHAUST;
        case ACQUIRE:
            return SystemState.INTAKE;
        default:
            setWristUp();
            // for the first second of the idle state, intake with the motor
            // to make sure we get a good grip on the gear
            if (timeInState < 1) {
                mMasterTalon.set(kIntakeGearSetpoint);
            } else {
                mMasterTalon.set(0.0);
            }
            return SystemState.STOWED;
        }
    }

    private boolean mWristUp = false;

    public void setOpenLoop(double value) {
        mMasterTalon.set(value);
    }

    private void setWristUp() {
        mWristUp = true;
        mWristSolenoid.set(mWristUp);
    }

    private void setWristDown() {
        mWristUp = false;
        mWristSolenoid.set(mWristUp);
    }

    public synchronized void setWantedState(WantedState wanted) {
        mWantedState = wanted;
    }

    public synchronized void reset() {
        mWantedState = WantedState.ACQUIRE;
        mSystemState = SystemState.STOWED;
    }

    public boolean checkSystem() {
        System.out.println("Testing GearGrabber.--------------------------------");
        final double kCurrentThres = 0.5;

        mMasterTalon.changeControlMode(CANTalon.TalonControlMode.Voltage);

        mMasterTalon.set(-6.0f);
        Timer.delay(4.0);
        final double current = mMasterTalon.getOutputCurrent();
        mMasterTalon.set(0.0);

        System.out.println("MotorGearGrabber Current: " + current);

        if (current < kCurrentThres) {
            System.out.println("!!!!!!!!!!!! MotorGear Grabber Current Low !!!!!!!!!!!");
            return false;
        }
        return true;
    }

}
