package com.team254.lib.util.motion;

import static org.junit.Assert.*;

import org.junit.Test;

public class SetpointGeneratorTest {

    public MotionState followProfile(SetpointGenerator spg, MotionProfileConstraints constraints,
            MotionProfileGoal goal, MotionState start_state, double dt, int max_iterations) {
        MotionState prev_state = start_state;

        System.out.println("Goal: " + goal);
        System.out.println("Start state: " + prev_state);
        int i = 0;
        for (; i < max_iterations; ++i) {
            SetpointGenerator.Setpoint setpoint = spg.getSetpoint(constraints, goal, prev_state, prev_state.t() + dt);
            prev_state = setpoint.motion_state;
            System.out.println(prev_state);
            if (setpoint.final_setpoint) {
                System.out.println("Goal reached");
                break;
            }
        }
        if (i == max_iterations) {
            System.out.println("Iteration limit reached");
        }
        return prev_state;
    }

    @Test
    public void testStationaryToStationary() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(10.0, 10.0);
        MotionProfileGoal goal = new MotionProfileGoal(100.0);
        MotionState start_state = new MotionState(0.0, 0.0, 0.0, 0.0);
        final double dt = 0.01;

        SetpointGenerator spg = new SetpointGenerator();
        MotionState final_setpoint = followProfile(spg, constraints, goal, start_state, dt, 1500);
        assertTrue(goal.atGoalState(final_setpoint));
    }

    @Test
    public void testUpdateGoal() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(10.0, 10.0);
        MotionProfileGoal goal = new MotionProfileGoal(100.0);
        MotionState start_state = new MotionState(0.0, 0.0, 0.0, 0.0);
        final double dt = 0.01;

        SetpointGenerator spg = new SetpointGenerator();
        MotionState final_setpoint = followProfile(spg, constraints, goal, start_state, dt, 500);
        assertTrue(!goal.atGoalState(final_setpoint));

        goal = new MotionProfileGoal(0.0);
        final_setpoint = followProfile(spg, constraints, goal, final_setpoint, dt, 1000);
        assertTrue(goal.atGoalState(final_setpoint));
    }

    @Test
    public void testUpdateState() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(10.0, 10.0);
        MotionProfileGoal goal = new MotionProfileGoal(100.0);
        MotionState start_state = new MotionState(0.0, 0.0, 0.0, 0.0);
        final double dt = 0.01;

        SetpointGenerator spg = new SetpointGenerator();
        MotionState final_setpoint = followProfile(spg, constraints, goal, start_state, dt, 500);
        assertTrue(!goal.atGoalState(final_setpoint));

        start_state = new MotionState(5.0, 50.0, 0.0, 0.0);
        final_setpoint = followProfile(spg, constraints, goal, start_state, dt, 1500);
        assertTrue(goal.atGoalState(final_setpoint));
    }

    @Test
    public void testUpdateConstraints() {
        MotionProfileConstraints constraints = new MotionProfileConstraints(10.0, 10.0);
        MotionProfileGoal goal = new MotionProfileGoal(100.0);
        MotionState start_state = new MotionState(0.0, 0.0, 0.0, 0.0);
        final double dt = 0.01;

        SetpointGenerator spg = new SetpointGenerator();
        MotionState final_setpoint = followProfile(spg, constraints, goal, start_state, dt, 500);
        assertTrue(!goal.atGoalState(final_setpoint));

        constraints = new MotionProfileConstraints(20.0, 20.0);
        final_setpoint = followProfile(spg, constraints, goal, final_setpoint, dt, 1500);
        assertTrue(goal.atGoalState(final_setpoint));
    }
}
