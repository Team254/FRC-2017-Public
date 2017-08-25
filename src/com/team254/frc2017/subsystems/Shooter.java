package com.team254.frc2017.subsystems;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import com.ctre.CANTalon;

import com.team254.frc2017.Constants;
import com.team254.frc2017.RobotState;
import com.team254.frc2017.ShooterAimingParameters;
import com.team254.frc2017.loops.Loop;
import com.team254.frc2017.loops.Looper;
import com.team254.lib.util.CircularBuffer;
import com.team254.lib.util.ReflectingCSVWriter;
import com.team254.lib.util.Util;
import com.team254.lib.util.drivers.CANTalonFactory;

import java.util.Arrays;
import java.util.Optional;

/**
 * The shooter subsystem consists of 4 775 Pro motors driving twin backspin flywheels. When run in reverse, these motors
 * power the robot's climber through a 1 way bearing. The shooter subsystem goes through 3 stages when shooting. 
 * 1. Spin Up 
 *  Use a PIDF controller to spin up to the desired RPM. We acquire this desired RPM by converting the camera's range
 *  value into an RPM value using the range map in the {@link Constants} class. 
 * 2. Hold When Ready
 *  Once the flywheel's
 *  RPM stabilizes (remains within a certain bandwidth for certain amount of time), the shooter switches to the hold when
 *  ready stage. In this stage, we collect kF samples. The idea is that we want to run the shooter in open loop when we
 *  start firing, so in this stage we calculate the voltage we need to supply to spin the flywheel at the desired RPM. 
 * 3. Hold 
 *  Once we collect enough kF samples, the shooter switches to the hold stage. This is the stage that we begin
 *  firing balls. We set kP, kI, and kD all to 0 and use the kF value we calculated in the previous stage for essentially
 *  open loop control. The reason we fire in open loop is that we found it creates a much narrower stream and leads to
 *  smaller RPM drops between fuel shots.
 * 
 * @see Subsystem.java
 */
public class Shooter extends Subsystem {
    private static Shooter mInstance = null;

    public static class ShooterDebugOutput {
        public double timestamp;
        public double setpoint;
        public double rpm;
        public double voltage;
        public ControlMethod control_method;
        public double kF;
        public double range;
    }

    public static int kSpinUpProfile = 0;
    public static int kHoldProfile = 1;

    public static Shooter getInstance() {
        if (mInstance == null) {
            mInstance = new Shooter();
        }
        return mInstance;
    }

    public enum ControlMethod {
        OPEN_LOOP, // open loop voltage control for running the climber
        SPIN_UP, // PIDF to desired RPM
        HOLD_WHEN_READY, // calculate average kF
        HOLD, // switch to pure kF control
    }

    private final CANTalon mRightMaster, mRightSlave, mLeftSlave1, mLeftSlave2;

    private ControlMethod mControlMethod;
    private double mSetpointRpm;
    private double mLastRpmSpeed;

    private CircularBuffer mKfEstimator = new CircularBuffer(Constants.kShooterKfBufferSize);

    // Used for transitioning from spin-up to hold loop.
    private boolean mOnTarget = false;
    private double mOnTargetStartTime = Double.POSITIVE_INFINITY;

    private ShooterDebugOutput mDebug = new ShooterDebugOutput();

    private final ReflectingCSVWriter<ShooterDebugOutput> mCSVWriter;

    private Shooter() {
        mRightMaster = CANTalonFactory.createDefaultTalon(Constants.kRightShooterMasterId);
        mRightMaster.changeControlMode(CANTalon.TalonControlMode.Voltage);
        mRightMaster.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Relative);
        mRightMaster.reverseSensor(true);
        mRightMaster.reverseOutput(false);
        mRightMaster.enableBrakeMode(false);
        mRightMaster.SetVelocityMeasurementPeriod(CANTalon.VelocityMeasurementPeriod.Period_10Ms);
        mRightMaster.SetVelocityMeasurementWindow(32);
        mRightMaster.setNominalClosedLoopVoltage(12);

        mRightMaster.setStatusFrameRateMs(CANTalon.StatusFrameRate.General, 2);
        mRightMaster.setStatusFrameRateMs(CANTalon.StatusFrameRate.AnalogTempVbat, 2);

        CANTalon.FeedbackDeviceStatus sensorPresent = mRightMaster
                .isSensorPresent(CANTalon.FeedbackDevice.CtreMagEncoder_Relative);
        if (sensorPresent != CANTalon.FeedbackDeviceStatus.FeedbackStatusPresent) {
            DriverStation.reportError("Could not detect shooter encoder: " + sensorPresent, false);
        }

        mRightSlave = makeSlave(Constants.kRightShooterSlaveId, false);
        mLeftSlave1 = makeSlave(Constants.kLeftShooterSlave1Id, true);
        mLeftSlave2 = makeSlave(Constants.kLeftShooterSlave2Id, true);

        refreshControllerConsts();

        mControlMethod = ControlMethod.OPEN_LOOP;

        System.out.println("RPM Polynomial: " + Constants.kFlywheelAutoAimPolynomial);

        mCSVWriter = new ReflectingCSVWriter<ShooterDebugOutput>("/home/lvuser/SHOOTER-LOGS.csv",
                ShooterDebugOutput.class);
    }

    /**
     * Load PIDF profiles onto the master talon
     */
    public void refreshControllerConsts() {
        mRightMaster.setProfile(kSpinUpProfile);
        mRightMaster.setP(Constants.kShooterTalonKP);
        mRightMaster.setI(Constants.kShooterTalonKI);
        mRightMaster.setD(Constants.kShooterTalonKD);
        mRightMaster.setF(Constants.kShooterTalonKF);
        mRightMaster.setIZone(Constants.kShooterTalonIZone);

        mRightMaster.setProfile(kHoldProfile);
        mRightMaster.setP(0.0);
        mRightMaster.setI(0.0);
        mRightMaster.setD(0.0);
        mRightMaster.setF(Constants.kShooterTalonKF);
        mRightMaster.setIZone(0);

        mRightMaster.setVoltageRampRate(Constants.kShooterRampRate);
    }

    @Override
    public synchronized void outputToSmartDashboard() {
        double current_rpm = getSpeedRpm();
        SmartDashboard.putNumber("shooter_speed_talon", current_rpm);
        SmartDashboard.putNumber("shooter_speed_error", mSetpointRpm - current_rpm);
        SmartDashboard.putNumber("shooter_output_voltage", mRightMaster.getOutputVoltage());
        SmartDashboard.putNumber("shooter_setpoint", mSetpointRpm);

        SmartDashboard.putBoolean("shooter on target", isOnTarget());
        // SmartDashboard.putNumber("shooter_talon_position", mRightMaster.getPosition());
        // SmartDashboard.putNumber("shooter_talon_enc_position", mRightMaster.getEncPosition());
    }

    @Override
    public synchronized void stop() {
        setOpenLoop(0.0);
        mSetpointRpm = 0.0;
    }

    @Override
    public void zeroSensors() {
        // Don't zero the flywheel, it'll make deltas screwy
    }

    @Override
    public void registerEnabledLoops(Looper enabledLooper) {
        enabledLooper.register(new Loop() {
            @Override
            public void onStart(double timestamp) {
                synchronized (Shooter.this) {
                    mControlMethod = ControlMethod.OPEN_LOOP;
                    mKfEstimator.clear();
                    mOnTarget = false;
                    mOnTargetStartTime = Double.POSITIVE_INFINITY;
                }
            }

            @Override
            public void onLoop(double timestamp) {
                synchronized (Shooter.this) {
                    if (mControlMethod != ControlMethod.OPEN_LOOP) {
                        handleClosedLoop(timestamp);
                        mCSVWriter.add(mDebug);
                    } else {
                        // Reset all state.
                        mKfEstimator.clear();
                        mOnTarget = false;
                        mOnTargetStartTime = Double.POSITIVE_INFINITY;
                    }
                }
            }

            @Override
            public void onStop(double timestamp) {
                mCSVWriter.flush();
            }
        });
    }

    /**
     * Run the shooter in open loop, used for climbing
     */
    public synchronized void setOpenLoop(double voltage) {
        if (mControlMethod != ControlMethod.OPEN_LOOP) {
            mControlMethod = ControlMethod.OPEN_LOOP;
            mRightMaster.changeControlMode(CANTalon.TalonControlMode.Voltage);
            mRightMaster.setCurrentLimit(Constants.kShooterOpenLoopCurrentLimit);
            mRightMaster.EnableCurrentLimit(true);
        }
        mRightMaster.set(voltage);
    }

    /**
     * Put the shooter in spinup mode
     */
    public synchronized void setSpinUp(double setpointRpm) {
        if (mControlMethod != ControlMethod.SPIN_UP) {
            configureForSpinUp();
        }
        mSetpointRpm = setpointRpm;
    }

    /**
     * Put the shooter in hold when ready mode
     */
    public synchronized void setHoldWhenReady(double setpointRpm) {
        if (mControlMethod == ControlMethod.OPEN_LOOP || mControlMethod == ControlMethod.SPIN_UP) {
            configureForHoldWhenReady();
        }
        mSetpointRpm = setpointRpm;
    }

    /**
     * Configure talons for spin up
     */
    private void configureForSpinUp() {
        mControlMethod = ControlMethod.SPIN_UP;
        mRightMaster.changeControlMode(CANTalon.TalonControlMode.Speed);
        mRightMaster.setProfile(kSpinUpProfile);
        mRightMaster.EnableCurrentLimit(false);
        mRightMaster.DisableNominalClosedLoopVoltage();
        mRightMaster.setVoltageRampRate(Constants.kShooterRampRate);
    }

    /**
     * Configure talons for hold when ready
     */
    private void configureForHoldWhenReady() {
        mControlMethod = ControlMethod.HOLD_WHEN_READY;
        mRightMaster.changeControlMode(CANTalon.TalonControlMode.Speed);
        mRightMaster.setProfile(kSpinUpProfile);
        mRightMaster.EnableCurrentLimit(false);
        mRightMaster.DisableNominalClosedLoopVoltage();
        mRightMaster.setVoltageRampRate(Constants.kShooterRampRate);
    }

    /**
     * Configure talons for hold
     */
    private void configureForHold() {
        mControlMethod = ControlMethod.HOLD;
        mRightMaster.changeControlMode(CANTalon.TalonControlMode.Speed);
        mRightMaster.setProfile(kHoldProfile);
        mRightMaster.EnableCurrentLimit(false);
        mRightMaster.setNominalClosedLoopVoltage(12.0);
        mRightMaster.setF(mKfEstimator.getAverage());
        mRightMaster.setVoltageRampRate(Constants.kShooterHoldRampRate);
    }

    private void resetHold() {
        mKfEstimator.clear();
        mOnTarget = false;
    }

    /**
     * Estimate the kF value from current RPM and voltage
     */
    private double estimateKf(double rpm, double voltage) {
        final double speed_in_ticks_per_100ms = 4096.0 / 600.0 * rpm;
        final double output = 1023.0 / 12.0 * voltage;
        return output / speed_in_ticks_per_100ms;
    }

    /**
     * Main control loop of the shooter. This method will progress the shooter through the spin up, hold when ready, and
     * hold stages.
     */
    private void handleClosedLoop(double timestamp) {
        final double speed = getSpeedRpm();
        final double voltage = mRightMaster.getOutputVoltage();
        mLastRpmSpeed = speed;

        // See if we should be spinning up or holding.
        if (mControlMethod == ControlMethod.SPIN_UP) {
            mRightMaster.set(mSetpointRpm);
            resetHold();
        } else if (mControlMethod == ControlMethod.HOLD_WHEN_READY) {
            final double abs_error = Math.abs(speed - mSetpointRpm);
            final boolean on_target_now = mOnTarget ? abs_error < Constants.kShooterStopOnTargetRpm
                    : abs_error < Constants.kShooterStartOnTargetRpm;
            if (on_target_now && !mOnTarget) {
                // First cycle on target.
                mOnTargetStartTime = timestamp;
                mOnTarget = true;
            } else if (!on_target_now) {
                resetHold();
            }

            if (mOnTarget) {
                // Update Kv.
                mKfEstimator.addValue(estimateKf(speed, voltage));
            }
            if (mKfEstimator.getNumValues() >= Constants.kShooterMinOnTargetSamples) {
                configureForHold();
            } else {
                mRightMaster.set(mSetpointRpm);
            }
        }
        // No else because we may have changed control methods above.
        if (mControlMethod == ControlMethod.HOLD) {
            // Update Kv if we exceed our target velocity. As the system heats up, drag is reduced.
            if (speed > mSetpointRpm) {
                mKfEstimator.addValue(estimateKf(speed, voltage));
                mRightMaster.setF(mKfEstimator.getAverage());
            }
        }
        mDebug.timestamp = timestamp;
        mDebug.rpm = speed;
        mDebug.setpoint = mSetpointRpm;
        mDebug.voltage = voltage;
        mDebug.control_method = mControlMethod;
        mDebug.kF = mKfEstimator.getAverage();
        Optional<ShooterAimingParameters> params = RobotState.getInstance().getAimingParameters();
        if (params.isPresent()) {
            mDebug.range = params.get().getRange();
        } else {
            mDebug.range = 0;
        }
    }

    public synchronized double getSetpointRpm() {
        return mSetpointRpm;
    }

    private double getSpeedRpm() {
        return mRightMaster.getSpeed();
    }

    private static CANTalon makeSlave(int talonId, boolean flipOutput) {
        CANTalon slave = CANTalonFactory.createPermanentSlaveTalon(talonId, Constants.kRightShooterMasterId);
        slave.reverseOutput(flipOutput);
        slave.enableBrakeMode(false);
        return slave;
    }

    public synchronized boolean isOnTarget() {
        return mControlMethod == ControlMethod.HOLD;
    }

    public synchronized double getLastSpeedRpm() {
        return mLastRpmSpeed;
    }

    @Override
    public void writeToLog() {
        mCSVWriter.write();
    }

    public boolean checkSystem() {
        System.out.println("Testing SHOOTER.----------------------------------------");
        final double kCurrentThres = 0.5;
        final double kRpmThres = 1200;

        mRightMaster.changeControlMode(CANTalon.TalonControlMode.Voltage);
        mRightSlave.changeControlMode(CANTalon.TalonControlMode.Voltage);
        mLeftSlave1.changeControlMode(CANTalon.TalonControlMode.Voltage);
        mLeftSlave2.changeControlMode(CANTalon.TalonControlMode.Voltage);

        mRightMaster.set(6.0f);
        Timer.delay(4.0);
        final double currentRightMaster = mRightMaster.getOutputCurrent();
        final double rpmMaster = mRightMaster.getSpeed();
        mRightMaster.set(0.0f);

        Timer.delay(2.0);

        mRightSlave.set(6.0f);
        Timer.delay(4.0);
        final double currentRightSlave = mRightSlave.getOutputCurrent();
        final double rpmRightSlave = mRightMaster.getSpeed();
        mRightSlave.set(0.0f);

        Timer.delay(2.0);

        mLeftSlave1.set(-6.0f);
        Timer.delay(4.0);
        final double currentLeftSlave1 = mLeftSlave1.getOutputCurrent();
        final double rpmLeftSlave1 = mRightMaster.getSpeed();
        mLeftSlave1.set(0.0f);

        Timer.delay(2.0);

        mLeftSlave2.set(-6.0f);
        Timer.delay(4.0);
        final double currentLeftSlave2 = mLeftSlave2.getOutputCurrent();
        final double rpmLeftSlave2 = mRightMaster.getSpeed();
        mLeftSlave2.set(0.0f);

        mRightSlave.changeControlMode(CANTalon.TalonControlMode.Follower);
        mLeftSlave1.changeControlMode(CANTalon.TalonControlMode.Follower);
        mLeftSlave2.changeControlMode(CANTalon.TalonControlMode.Follower);

        mRightSlave.set(Constants.kRightShooterMasterId);
        mLeftSlave1.set(Constants.kRightShooterMasterId);
        mLeftSlave2.set(Constants.kRightShooterMasterId);

        System.out.println("Shooter Right Master Current: " + currentRightMaster + " Shooter Right Slave Current: "
                + currentRightSlave);
        System.out.println("Shooter Left Slave One Current: " + currentLeftSlave1 + " Shooter Left Slave Two Current: "
                + currentLeftSlave2);
        System.out.println("Shooter RPM Master: " + rpmMaster + " RPM Right slave: " + rpmRightSlave
                + " RPM Left Slave 1: " + rpmLeftSlave1 + " RPM Left Slave 2: " + rpmLeftSlave2);

        boolean failure = false;

        if (currentRightMaster < kCurrentThres) {
            failure = true;
            System.out.println("!!!!!!!!!!!!!!!!!! Shooter Right Master Current Low !!!!!!!!!!");
        }

        if (currentRightSlave < kCurrentThres) {
            failure = true;
            System.out.println("!!!!!!!!!!!!!!!!!! Shooter Right Slave Current Low !!!!!!!!!!");
        }

        if (currentLeftSlave1 < kCurrentThres) {
            failure = true;
            System.out.println("!!!!!!!!!!!!!!!!!! Shooter Left Slave One Current Low !!!!!!!!!!");
        }

        if (currentLeftSlave2 < kCurrentThres) {
            failure = true;
            System.out.println("!!!!!!!!!!!!!!!!!! Shooter Left Slave Two Current Low !!!!!!!!!!");
        }

        if (!Util.allCloseTo(Arrays.asList(currentRightMaster, currentRightSlave, currentLeftSlave1,
                currentLeftSlave2), currentRightMaster, 5.0)) {
            failure = true;
            System.out.println("!!!!!!!!!!!!!!!!!! Shooter currents different !!!!!!!!!!!!!!!!!");
        }

        if (rpmMaster < kRpmThres) {
            failure = true;
            System.out.println("!!!!!!!!!!!!!!!!!! Shooter Master RPM Low !!!!!!!!!!!!!!!!!!!!!!!");
        }

        if (rpmRightSlave < kRpmThres) {
            failure = true;
            System.out.println("!!!!!!!!!!!!!!!!!! Shooter Right Slave RPM Low !!!!!!!!!!!!!!!!!!!!!!!");
        }

        if (rpmLeftSlave1 < kRpmThres) {
            failure = true;
            System.out.println("!!!!!!!!!!!!!!!!!! Shooter Left Slave1 RPM Low !!!!!!!!!!!!!!!!!!!!!!!");
        }

        if (rpmLeftSlave2 < kRpmThres) {
            failure = true;
            System.out.println("!!!!!!!!!!!!!!!!!! Shooter Left Slave2 RPM Low !!!!!!!!!!!!!!!!!!!!!!!");
        }

        if (!Util.allCloseTo(Arrays.asList(rpmMaster, rpmRightSlave, rpmLeftSlave1, rpmLeftSlave2), rpmMaster, 250)) {
            failure = true;
            System.out.println("!!!!!!!!!!!!!!!!!! Shooter RPMs different !!!!!!!!!!!!!!!!!!!!!!!");
        }

        return !failure;
    }
}
