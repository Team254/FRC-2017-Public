package com.team254.lib.util.motion;

import com.team254.lib.util.math.Rotation2d;

/**
 * Class to deal with angle wrapping for following a heading profile. All states are assumed to be in units of degrees,
 * and wrap on the interval of [-180, 180].
 */
public class HeadingProfileFollower extends ProfileFollower {

    public HeadingProfileFollower(double kp, double ki, double kv, double kffv, double kffa) {
        super(kp, ki, kv, kffv, kffa);
    }

    @Override
    public double update(MotionState latest_state, double t) {
        final Rotation2d goal_rotation_inverse = Rotation2d.fromDegrees(mGoal.pos()).inverse();
        // Update both the setpoint and latest state to be relative to the new goal.
        if (mLatestSetpoint != null) {
            mLatestSetpoint.motion_state = new MotionState(mLatestSetpoint.motion_state.t(),
                    mGoal.pos() + goal_rotation_inverse
                            .rotateBy(Rotation2d.fromDegrees(mLatestSetpoint.motion_state.pos())).getDegrees(),
                    mLatestSetpoint.motion_state.vel(), mLatestSetpoint.motion_state.acc());
        }
        final MotionState latest_state_unwrapped = new MotionState(latest_state.t(),
                mGoal.pos() + goal_rotation_inverse.rotateBy(Rotation2d.fromDegrees(latest_state.pos())).getDegrees(),
                latest_state.vel(), latest_state.acc());
        double result = super.update(latest_state_unwrapped, t);
        // Reset the integrator when we are close to the goal (encourage stiction!).
        if (Math.abs(latest_state_unwrapped.pos() - mGoal.pos()) < mGoal.pos_tolerance()) {
            result = 0.0;
            super.resetIntegral();
        }
        return result;
    }

    /**
     * Convert a motion state representing an angle to a properly wrapped angle.
     */
    public static MotionState canonicalize(MotionState state) {
        return new MotionState(state.t(), Rotation2d.fromDegrees(state.pos()).getDegrees(), state.vel(), state.acc());
    }
}
