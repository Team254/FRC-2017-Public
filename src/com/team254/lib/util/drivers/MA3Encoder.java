package com.team254.lib.util.drivers;

import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Notifier;

import com.team254.lib.util.CrashTrackingRunnable;
import com.team254.lib.util.math.Rotation2d;

/**
 * A 12-bit PWM MA3 absolute encoder. http://cdn.usdigital.com/assets/datasheets/MA3_datasheet.pdf
 */
public class MA3Encoder {
    static final double kNominalPeriodS = 4098 * 1E-6;
    static final double kPeriodToleranceS = 200 * 1E-6;

    protected DigitalInput digital_input_;
    protected Counter high_counter_; // access only from inner class after
                                     // construction
    protected Counter period_counter_; // access only from inner class after
                                       // construction
    protected Notifier notifier_;
    protected Rotation2d rotation_ = new Rotation2d();
    protected Rotation2d home_ = new Rotation2d();
    protected int num_rotations_ = 0;
    protected boolean error_ = false;

    private CrashTrackingRunnable read_thread_ = new CrashTrackingRunnable() {
        @Override
        public void runCrashTracked() {
            if (high_counter_.getStopped()) {
                if (!error_) {
                    DriverStation.reportError("No MA3Encoder on channel " + digital_input_.getChannel(), false);
                }
                error_ = true;
                return;
            }
            error_ = false;
            double t_high = high_counter_.getPeriod();
            double t_total = period_counter_.getPeriod();
            if (t_total > kNominalPeriodS + kPeriodToleranceS || t_total < kNominalPeriodS - kPeriodToleranceS) {
                // We got a nonsensical rising-to-rising edge period, so ignore
                // this sample.
                return;
            }
            double x = (t_high * 4098) / (t_total) - 1;
            if (x > 4095) {
                x = 4095;
            }
            Rotation2d new_rotation = Rotation2d.fromRadians(2 * Math.PI * x / 4096);

            // Check for rollover
            synchronized (MA3Encoder.this) {
                double relative_angle = rotation_.getRadians()
                        + rotation_.inverse().rotateBy(new_rotation).getRadians();
                if (relative_angle > Math.PI) {
                    ++num_rotations_;
                } else if (relative_angle < -Math.PI) {
                    --num_rotations_;
                }
                rotation_ = new_rotation;
            }
        }
    };

    public MA3Encoder(int port) {
        digital_input_ = new DigitalInput(port);
        high_counter_ = new Counter(digital_input_);
        period_counter_ = new Counter(digital_input_);
        high_counter_.setSamplesToAverage(1);
        high_counter_.setSemiPeriodMode(true);
        period_counter_.setSamplesToAverage(1);
        notifier_ = new Notifier(read_thread_);
        notifier_.startPeriodic(0.01); // 100 Hz
    }

    public synchronized Rotation2d getCalibratedAngle() {
        return home_.rotateBy(rotation_);
    }

    public synchronized void zero() {
        num_rotations_ = 0;
        home_ = rotation_.inverse();
    }

    public synchronized Rotation2d getRawAngle() {
        return rotation_;
    }

    public synchronized double getContinuousAngleDegrees() {
        return getRawAngle().getDegrees() + num_rotations_ * 360.0 + home_.getDegrees();
    }

}
