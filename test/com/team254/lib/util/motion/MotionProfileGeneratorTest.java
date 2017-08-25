package com.team254.lib.util.motion;

import static org.junit.Assert.*;

import org.junit.Test;

import com.team254.lib.util.motion.MotionProfileGoal.CompletionBehavior;

import static com.team254.lib.util.TestUtil.*;
import static com.team254.lib.util.motion.MotionUtil.*;

public class MotionProfileGeneratorTest {

    protected static void validateProfile(MotionProfileConstraints constraints, MotionProfileGoal goal,
            MotionState start, MotionProfile profile) {
        // Profile should be valid.
        assertTrue(profile.isValid());
        // Profile should start at the start state (clamped to accel and velocity limits).
        assertThat(profile.startState().t(), epsilonEqualTo(start.t(), kEpsilon));
        assertThat(profile.startState().pos(), epsilonEqualTo(start.pos(), kEpsilon));
        assertThat(profile.startState().vel(), epsilonEqualTo(
                Math.signum(start.vel()) * Math.min(Math.abs(start.vel()), constraints.max_abs_vel()), kEpsilon));
        // Profile should not violate constraints.
        for (MotionSegment s : profile.segments()) {
            assertTrue(Math.abs(s.start().vel()) <= constraints.max_abs_vel());
            assertTrue(Math.abs(s.end().vel()) <= constraints.max_abs_vel());
            if (goal.completion_behavior() != CompletionBehavior.VIOLATE_MAX_ACCEL) {
                assertTrue(Math.abs(s.start().acc()) <= constraints.max_abs_acc());
                assertTrue(Math.abs(s.end().acc()) <= constraints.max_abs_acc());
            }
        }
        // Profile should end at the goal state.
        if (goal.completion_behavior() != CompletionBehavior.VIOLATE_MAX_ABS_VEL) {
            assertTrue(goal.atGoalState(profile.endState()));
        } else {
            assertTrue(goal.atGoalPos(profile.endPos()));
        }
    }

    protected static void testProfile(MotionProfileConstraints constraints, MotionProfileGoal goal, MotionState start,
            double expected_duration, double expected_length) {
        MotionProfile positive_profile = MotionProfileGenerator.generateProfile(constraints, goal, start);
        assertThat(positive_profile.toString(), positive_profile.duration(),
                epsilonEqualTo(expected_duration, kEpsilon));
        assertThat(positive_profile.toString(), positive_profile.length(), epsilonEqualTo(expected_length, kEpsilon));
        validateProfile(constraints, goal, start, positive_profile);

        MotionProfile negative_profile = MotionProfileGenerator.generateProfile(constraints, goal.flipped(),
                start.flipped());
        assertThat(negative_profile.toString(), negative_profile.duration(),
                epsilonEqualTo(expected_duration, kEpsilon));
        assertThat(negative_profile.toString(), negative_profile.length(), epsilonEqualTo(expected_length, kEpsilon));
        validateProfile(constraints, goal.flipped(), start.flipped(), negative_profile);
    }

    @Test
    public void testAlreadyFinished() {
        // No initial velocity.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(0.0),
                new MotionState(0.0, 0.0, 0.0, 0.0), 0.0, 0.0);
        // Initial velocity matches goal state.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(2.0, 5.0),
                new MotionState(0.0, 2.0, 5.0, 7.0), 0.0, 0.0);
        // Initial velocity is too high, so allow infinite accel.
        testProfile(new MotionProfileConstraints(10.0, 10.0),
                new MotionProfileGoal(2.0, 5.0, CompletionBehavior.VIOLATE_MAX_ABS_VEL),
                new MotionState(0.0, 2.0, 9.0, 7.0), 0.0, 0.0);
        // Initial velocity is too high, so allow infinite accel.
        testProfile(new MotionProfileConstraints(10.0, 10.0),
                new MotionProfileGoal(2.0, 5.0, CompletionBehavior.VIOLATE_MAX_ACCEL),
                new MotionState(0.0, 2.0, 9.0, 7.0), 0.0, 0.0);
    }

    @Test
    public void testStationaryToStationary() {
        // Trapezoidal move.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(100.0),
                new MotionState(0.0, 0.0, 0.0, 0.0), 11.0, 100.0);
        // Triangle move.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(10.0),
                new MotionState(0.0, 0.0, 0.0, 0.0), 2.0, 10.0);
    }

    @Test
    public void testMovingTowardsToStationary() {
        // Moving towards goal, trapezoidal move.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(100.0),
                new MotionState(0.0, 0.0, 10.0, 0.0), 10.5, 100.0);
        // Moving towards goal, triangle move.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(10.0),
                new MotionState(0.0, 0.0, 5.0, 0.0), 1.625, 10.0);
        // Moving towards goal, cruise and stop.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(10.0),
                new MotionState(0.0, 0.0, 10.0, 0.0), 1.5, 10.0);
        // Moving towards goal, overshoot and come back.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(0.0),
                new MotionState(0.0, 0.0, 10.0, 0.0), 1.0 + Math.sqrt(2.0), 10.0);
        // Moving towards goal, violate max vel.
        testProfile(new MotionProfileConstraints(10.0, 10.0),
                new MotionProfileGoal(1.0, 0.0, CompletionBehavior.VIOLATE_MAX_ABS_VEL),
                new MotionState(0.0, 0.0, 10.0, 0.0), (10.0 - Math.sqrt(80.0)) / 10.0, 1.0);
        // Moving towards goal, violate max accel.
        testProfile(new MotionProfileConstraints(10.0, 10.0),
                new MotionProfileGoal(1.0, 0.0, CompletionBehavior.VIOLATE_MAX_ACCEL),
                new MotionState(0.0, 0.0, 10.0, 0.0), 1.0 / 5.0, 1.0);
    }

    @Test
    public void testMovingAwayToStationary() {
        // Moving away from goal, trapezoidal move.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(100.0),
                new MotionState(0.0, 0.0, -10.0, 0.0), 12.5, 110.0);
        // Moving away from goal, triangle move.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(5.0),
                new MotionState(0.0, 0.0, -10.0, 0.0), 3.0, 15.0);
    }

    @Test
    public void testStationaryToMoving() {
        // Accelerate and cruise.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(100.0, 10.0),
                new MotionState(0.0, 0.0, 0.0, 0.0), 10.5, 100.0);

        // Trapezoidal move.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(100.0, 5.0),
                new MotionState(0.0, 0.0, 0.0, 0.0), 10.625, 100.0);

        // Pure acceleration.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(1.0, 10.0),
                new MotionState(0.0, 0.0, 0.0, 0.0), Math.sqrt(1.0 / 5.0), 1.0);

        // Triangle move.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(8.75, 5.0),
                new MotionState(0.0, 0.0, 0.0, 0.0), 1.5, 8.75);
    }

    @Test
    public void testMovingTowardsToMoving() {
        // Moving towards goal, pure acceleration.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(3.75, 10.0),
                new MotionState(0.0, 0.0, 5.0, 0.0), 0.5, 3.75);
        // Moving towards goal, pure deceleration.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(10.0, 5.0),
                new MotionState(0.0, 0.0, 10.0, 0.0), 1.125, 10.0);
        // Moving towards goal, cruise.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(100.0, 10.0),
                new MotionState(0.0, 0.0, 10.0, 0.0), 10.0, 100.0);
        // Moving towards goal, accelerate and cruise.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(100.0, 10.0),
                new MotionState(0.0, 0.0, 5.0, 0.0), 10.125, 100.0);
        // Moving towards goal, trapezoidal move.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(100.0, 5.0),
                new MotionState(0.0, 0.0, 5.0, 0.0), 10.25, 100.0);
        // Moving towards goal, triangle move.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(1.0, 10.0),
                new MotionState(0.0, 0.0, 4.0, 0.0), 0.2, 1.0);
        // Moving towards goal, cruise and decelerate.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(10.0, 5.0),
                new MotionState(0.0, 0.0, 10.0, 0.0), 1.125, 10.0);
        // Moving towards goal, violate max vel.
        testProfile(new MotionProfileConstraints(10.0, 10.0),
                new MotionProfileGoal(1.0, 1.0, CompletionBehavior.VIOLATE_MAX_ABS_VEL),
                new MotionState(0.0, 0.0, 10.0, 0.0), (10.0 - Math.sqrt(80.0)) / 10.0, 1.0);
        // Moving towards goal, violate max accel.
        testProfile(new MotionProfileConstraints(10.0, 10.0),
                new MotionProfileGoal(1.0, 2.0, CompletionBehavior.VIOLATE_MAX_ACCEL),
                new MotionState(0.0, 0.0, 10.0, 0.0), 1.0 / 6.0, 1.0);
    }

    @Test
    public void testMovingAwayToMoving() {
        // Moving away from goal, stop and accelerate.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(1.0, 10.0),
                new MotionState(0.0, 0.0, -4.0, 0.0), 1.0, 2.6);
        // Moving away from goal, stop, accelerate, and cruise.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(100.0, 10.0),
                new MotionState(0.0, 0.0, -10.0, 0.0), 12.0, 110.0);
        // Moving away from goal, stop and trapezoid move.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(100.0, 5.0),
                new MotionState(0.0, 0.0, -10.0, 0.0), 12.125, 110.0);
        // Moving away from goal, stop and triangle move.
        testProfile(new MotionProfileConstraints(10.0, 10.0), new MotionProfileGoal(8.75, 5.0),
                new MotionState(0.0, 5.0, -10.0, 0.0), 2.5, 13.75);
    }

    @Test
    public void problematicCase1() {
        MotionProfile profile = MotionProfileGenerator.generateProfile(new MotionProfileConstraints(50.0, 25.0),
                new MotionProfileGoal(200.0), new MotionState(0.0, 0.0, 0.0, 0.0));
        System.out.println(profile);
        assertTrue(profile.firstStateByPos(160.0).get().vel() > 0.0);
    }
}
