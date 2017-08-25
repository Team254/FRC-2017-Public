package com.team254.lib.util.drivers;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Notifier;

import com.team254.lib.util.CrashTrackingRunnable;
import com.team254.lib.util.math.Rotation2d;

/**
 * A 10-bit analog MA3 absolute encoder. http://cdn.usdigital.com/assets/datasheets/MA3_datasheet.pdf
 */
public class MA3AnalogEncoder {

    private final AnalogInput mAnalogInput;

    protected Notifier notifier_;
    protected Rotation2d rotation_ = new Rotation2d();
    protected Rotation2d home_ = new Rotation2d();
    protected int num_rotations_ = 0;

    private CrashTrackingRunnable read_thread_ = new CrashTrackingRunnable() {
        @Override
        public void runCrashTracked() {
            Rotation2d new_rotation = Rotation2d.fromRadians(2 * Math.PI * mAnalogInput.getVoltage() / 5.0);

            // Check for rollover
            synchronized (MA3AnalogEncoder.this) {
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

    public MA3AnalogEncoder(int port) {
        mAnalogInput = new AnalogInput(port);
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
