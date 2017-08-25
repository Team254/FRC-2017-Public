package com.team254.lib.util.motion;

public class MotionTestUtil {

    protected abstract static class Dynamics {
        protected MotionState mState;

        public Dynamics(MotionState state) {
            mState = state;
        }

        public MotionState getState() {
            return mState;
        }

        public abstract void update(double command_vel, double dt);
    }

    protected static class IdealDynamics extends Dynamics {
        public IdealDynamics(MotionState state) {
            super(state);
        }

        @Override
        public void update(double command_vel, double dt) {
            final double acc = (command_vel - mState.vel()) / dt;
            mState = mState.extrapolate(mState.t() + dt, acc);
        }
    }

    protected static class ScaledDynamics extends Dynamics {
        protected double mVelRatio;

        public ScaledDynamics(MotionState state, double vel_ratio) {
            super(state);
            mVelRatio = vel_ratio;
        }

        @Override
        public void update(double command_vel, double dt) {
            final double acc = (command_vel * mVelRatio - mState.vel()) / dt;
            mState = mState.extrapolate(mState.t() + dt, acc);
        }
    }

    protected static class DeadbandDynamics extends Dynamics {
        protected double mDeadband;

        public DeadbandDynamics(MotionState state, double deadband) {
            super(state);
            mDeadband = deadband;
        }

        @Override
        public void update(double command_vel, double dt) {
            if (command_vel > -mDeadband && command_vel < mDeadband) {
                command_vel = 0.0;
            } else {
                command_vel = Math.signum(command_vel) * (Math.abs(command_vel) - mDeadband);
            }
            final double acc = (command_vel - mState.vel()) / dt;
            mState = mState.extrapolate(mState.t() + dt, acc);
        }
    }

    public static MotionState followProfile(ProfileFollower follower, Dynamics dynamics, double dt,
            int max_iterations) {
        int i = 0;
        for (; i < max_iterations && !follower.onTarget(); ++i) {
            MotionState state = dynamics.getState();
            final double t = state.t() + dt;
            final double command_vel = follower.update(state, t);
            dynamics.update(command_vel, dt);
            System.out.println("State: " + state + ", Pos error: " + follower.getPosError() + ", Vel error: "
                    + follower.getVelError() + ", Command: " + command_vel);
            if (follower.isFinishedProfile()) {
                System.out.println("Follower has finished profile");
            }
        }
        if (i == max_iterations) {
            System.out.println("Iteration limit reached");
        }
        System.out.println("Final state: " + dynamics.getState());
        return dynamics.getState();
    }
}
