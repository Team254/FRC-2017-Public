package com.team254.lib.util.motion;

import static org.junit.Assert.*;

import org.junit.Test;

import com.team254.lib.util.motion.MotionProfileGoal.CompletionBehavior;
import static com.team254.lib.util.motion.MotionTestUtil.*;

public class ProfileFollowerTest {

    @Test
    public void testStationaryToStationaryFeedforward() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(10.0, 10.0);
        MotionProfileGoal goal = new MotionProfileGoal(100.0);
        MotionState start_state = new MotionState(0.0, 0.0, 0.0, 0.0);
        final double dt = 0.01;

        ProfileFollower follower = new ProfileFollower(0.0, 0.0, 0.0, 1.0, 0.0);
        follower.setGoalAndConstraints(goal, constraints);
        MotionState final_state = followProfile(follower, new IdealDynamics(start_state), dt, 1500);
        assertTrue(goal.atGoalState(final_state));
    }

    @Test
    public void testStationaryToStationaryUpdateGoal() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(10.0, 10.0);
        MotionProfileGoal goal = new MotionProfileGoal(100.0);
        MotionState start_state = new MotionState(0.0, 0.0, 0.0, 0.0);
        final double dt = 0.01;

        ProfileFollower follower = new ProfileFollower(0.0, 0.0, 0.0, 1.0, 0.0);
        follower.setGoalAndConstraints(goal, constraints);
        Dynamics dynamics = new IdealDynamics(start_state);
        MotionState final_state = followProfile(follower, dynamics, dt, 500);
        assertFalse(goal.atGoalState(final_state));

        goal = new MotionProfileGoal(0.0);
        follower.setGoalAndConstraints(goal, constraints);
        final_state = followProfile(follower, dynamics, dt, 1500);
        assertTrue(goal.atGoalState(final_state));
    }

    @Test
    public void testStationaryToStationaryResetSetpoint() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(10.0, 10.0);
        MotionProfileGoal goal = new MotionProfileGoal(100.0);
        MotionState start_state = new MotionState(0.0, 0.0, 0.0, 0.0);
        final double dt = 0.01;

        ProfileFollower follower = new ProfileFollower(0.0, 0.0, 0.0, 1.0, 0.0);
        follower.setGoalAndConstraints(goal, constraints);
        Dynamics dynamics = new IdealDynamics(start_state);
        MotionState final_state = followProfile(follower, dynamics, dt, 500);
        assertFalse(goal.atGoalState(final_state));

        follower.resetSetpoint();
        final_state = followProfile(follower, dynamics, dt, 1500);
        assertTrue(goal.atGoalState(final_state));
    }

    @Test
    public void testStationaryToStationaryResetProfile() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(10.0, 10.0);
        MotionProfileGoal goal = new MotionProfileGoal(100.0);
        MotionState start_state = new MotionState(0.0, 0.0, 0.0, 0.0);
        final double dt = 0.01;

        ProfileFollower follower = new ProfileFollower(0.0, 0.0, 0.0, 1.0, 0.0);
        follower.setGoalAndConstraints(goal, constraints);
        Dynamics dynamics = new IdealDynamics(start_state);
        MotionState final_state = followProfile(follower, dynamics, dt, 500);
        assertFalse(goal.atGoalState(final_state));

        follower.resetProfile();
        follower.setGoalAndConstraints(goal, constraints);
        final_state = followProfile(follower, dynamics, dt, 1500);
        assertTrue(goal.atGoalState(final_state));
    }

    @Test
    public void testStationaryToStationaryFeedback() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(10.0, 10.0);
        MotionProfileGoal goal = new MotionProfileGoal(100.0, 0.0, CompletionBehavior.OVERSHOOT, 1.0, 1.0);
        MotionState start_state = new MotionState(0.0, 0.0, 0.0, 0.0);
        final double dt = 0.01;

        ProfileFollower follower = new ProfileFollower(0.5, 0.001, 0.5, 1.0, 0.1);
        follower.setGoalAndConstraints(goal, constraints);
        MotionState final_state = followProfile(follower, new ScaledDynamics(start_state, .8), dt, 2000);
        assertTrue(goal.atGoalState(final_state));
    }

    @Test
    public void testStationaryToStationaryFeedbackFast() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(10.0, 10.0);
        MotionProfileGoal goal = new MotionProfileGoal(100.0, 0.0, CompletionBehavior.OVERSHOOT, 1.0, 1.0);
        MotionState start_state = new MotionState(0.0, 0.0, 0.0, 0.0);
        final double dt = 0.01;

        ProfileFollower follower = new ProfileFollower(0.5, 0.001, 0.5, 1.0, 0.1);
        follower.setGoalAndConstraints(goal, constraints);
        MotionState final_state = followProfile(follower, new ScaledDynamics(start_state, 1.2), dt, 2000);
        assertTrue(goal.atGoalState(final_state));
    }

    @Test
    public void testStationaryToStationaryFeedbackDeadband() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(10.0, 10.0);
        MotionProfileGoal goal = new MotionProfileGoal(100.0, 0.0, CompletionBehavior.OVERSHOOT, 1.0, 1.0);
        MotionState start_state = new MotionState(0.0, 0.0, 0.0, 0.0);
        final double dt = 0.01;

        ProfileFollower follower = new ProfileFollower(0.5, 0.001, 0.5, 1.0, 0.1);
        follower.setGoalAndConstraints(goal, constraints);
        MotionState final_state = followProfile(follower, new DeadbandDynamics(start_state, 2.0), dt, 2000);
        assertTrue(goal.atGoalState(final_state));
    }

    @Test
    public void testStationaryToMovingOvershoot() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(10.0, 10.0);
        MotionProfileGoal goal = new MotionProfileGoal(-100.0, 10.0, CompletionBehavior.VIOLATE_MAX_ACCEL, 1.0, 0.1);
        MotionState start_state = new MotionState(0.0, 0.0, 0.0, 0.0);
        final double dt = 0.01;

        ProfileFollower follower = new ProfileFollower(0.0, 0.0, 0.0, 1.0, 0.0);
        follower.setGoalAndConstraints(goal, constraints);
        MotionState final_state = followProfile(follower, new ScaledDynamics(start_state, 1.2), dt, 2000);
        assertTrue(goal.atGoalPos(final_state.pos()));
    }
}
