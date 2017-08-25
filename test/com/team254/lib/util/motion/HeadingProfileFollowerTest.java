package com.team254.lib.util.motion;

import static org.junit.Assert.*;

import org.junit.Test;

import com.team254.lib.util.motion.MotionProfileGoal.CompletionBehavior;
import com.team254.lib.util.motion.MotionTestUtil.Dynamics;
import com.team254.lib.util.motion.MotionTestUtil.IdealDynamics;
import com.team254.lib.util.motion.MotionTestUtil.ScaledDynamics;

import static com.team254.lib.util.motion.MotionTestUtil.*;

public class HeadingProfileFollowerTest {

    @Test
    public void testStationaryToStationaryFeedforward() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(90.0, 90.0);
        MotionProfileGoal goal = new MotionProfileGoal(45.0); // 45 degrees
        MotionState start_state = new MotionState(0.0, 0.0, 0.0, 0.0); // 0 degrees
        final double dt = 0.01;

        ProfileFollower follower = new HeadingProfileFollower(0.0, 0.0, 0.0, 1.0, 0.0);
        follower.setGoalAndConstraints(goal, constraints);
        MotionState final_state = followProfile(follower, new IdealDynamics(start_state), dt, 1500);
        assertTrue(goal.atGoalState(HeadingProfileFollower.canonicalize(final_state)));
    }

    @Test
    public void testStationaryToStationaryFeedforwardWrap() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(90.0, 90.0);
        MotionProfileGoal goal = new MotionProfileGoal(45.0); // 45 degrees
        MotionState start_state = new MotionState(0.0, -179.0, 0.0, 0.0); // -179 degrees (==181 degrees)
        final double dt = 0.01;

        ProfileFollower follower = new HeadingProfileFollower(0.0, 0.0, 0.0, 1.0, 0.0);
        follower.setGoalAndConstraints(goal, constraints);
        MotionState final_state = followProfile(follower, new IdealDynamics(start_state), dt, 1500);
        assertTrue(goal.atGoalState(HeadingProfileFollower.canonicalize(final_state)));
    }

    @Test
    public void testStationaryToStationaryUpdateGoal() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(90.0, 90.0);
        MotionProfileGoal goal = new MotionProfileGoal(45.0); // 45 degrees
        MotionState start_state = new MotionState(0.0, -179.0, 0.0, 0.0); // -179 degrees (==181 degrees)
        final double dt = 0.01;

        ProfileFollower follower = new HeadingProfileFollower(0.0, 0.0, 0.0, 1.0, 0.0);
        follower.setGoalAndConstraints(goal, constraints);
        Dynamics dynamics = new IdealDynamics(start_state);
        MotionState final_state = followProfile(follower, dynamics, dt, 100);
        assertFalse(goal.atGoalState(HeadingProfileFollower.canonicalize(final_state)));

        goal = new MotionProfileGoal(-90.0); // -90 degrees
        follower.setGoalAndConstraints(goal, constraints);
        final_state = followProfile(follower, dynamics, dt, 1500);
        assertTrue(goal.atGoalState(HeadingProfileFollower.canonicalize(final_state)));
    }

    @Test
    public void testStationaryToStationaryUpdateGoalWrap() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(90.0, 90.0);
        MotionProfileGoal goal = new MotionProfileGoal(-179.0); // -179.0 degrees
        MotionState start_state = new MotionState(0.0, 0.0, 0.0, 0.0); // 0 degrees
        final double dt = 0.01;

        ProfileFollower follower = new HeadingProfileFollower(0.0, 0.0, 0.0, 1.0, 0.0);
        follower.setGoalAndConstraints(goal, constraints);
        Dynamics dynamics = new IdealDynamics(start_state);
        MotionState final_state = followProfile(follower, dynamics, dt, 100);
        assertFalse(goal.atGoalState(HeadingProfileFollower.canonicalize(final_state)));

        goal = new MotionProfileGoal(179.0); // 179 degrees degrees
        follower.setGoalAndConstraints(goal, constraints);
        final_state = followProfile(follower, dynamics, dt, 1500);
        assertTrue(goal.atGoalState(HeadingProfileFollower.canonicalize(final_state)));
    }

    @Test
    public void testStationaryToStationaryFeedback() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(90.0, 90.0);
        MotionProfileGoal goal = new MotionProfileGoal(100.0, 0.0, CompletionBehavior.OVERSHOOT, 1.0, 1.0);
        MotionState start_state = new MotionState(0.0, -179.0, 0.0, 0.0);
        final double dt = 0.01;

        ProfileFollower follower = new HeadingProfileFollower(0.5, 0.001, 0.5, 1.0, 0.1);
        follower.setGoalAndConstraints(goal, constraints);
        MotionState final_state = followProfile(follower, new ScaledDynamics(start_state, .8), dt, 2000);
        assertTrue(goal.atGoalState(HeadingProfileFollower.canonicalize(final_state)));
    }

    @Test
    public void testStationaryToStationaryFeedbackUpdateGoal() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(90.0, 90.0);
        MotionProfileGoal goal = new MotionProfileGoal(-179.0); // -179.0 degrees
        MotionState start_state = new MotionState(0.0, 0.0, 0.0, 0.0); // 0 degrees
        final double dt = 0.01;

        ProfileFollower follower = new HeadingProfileFollower(2.0, 0.001, 0.5, 1.0, 0.1);
        follower.setGoalAndConstraints(goal, constraints);
        Dynamics dynamics = new ScaledDynamics(start_state, .8);
        MotionState final_state = followProfile(follower, dynamics, dt, 100);
        assertFalse(goal.atGoalState(HeadingProfileFollower.canonicalize(final_state)));

        goal = new MotionProfileGoal(179.0);
        follower.setGoalAndConstraints(goal, constraints);

        final_state = followProfile(follower, dynamics, dt, 2000);
        assertTrue(goal.atGoalState(HeadingProfileFollower.canonicalize(final_state)));
    }
}
