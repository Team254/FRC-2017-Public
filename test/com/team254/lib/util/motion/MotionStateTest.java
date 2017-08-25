package com.team254.lib.util.motion;

import static org.junit.Assert.*;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;

public class MotionStateTest {

    @Test
    public void equals() {
        MotionState a = new MotionState(0.0, 0.0, 0.0, 0.0);
        MotionState b = new MotionState(0.0, 0.0, 0.0, 0.0);
        assertThat(b, is(equalTo(a)));

        final double kSmallEps = 1e-9;
        MotionState c = new MotionState(kSmallEps, kSmallEps, kSmallEps, kSmallEps);
        assertThat(c, is(equalTo(a)));
        assertTrue(a.equals(c, kSmallEps));

        final double kBigEps = 1e-3;
        MotionState d = new MotionState(kBigEps, kBigEps, kBigEps, kBigEps);
        assertTrue(a.equals(d, kBigEps));
        assertFalse(a.equals(d, kSmallEps));
        assertThat(d, is(not(equalTo(a))));
    }

    @Test
    public void extrapolateConstantVelocity() {
        // vel = 1
        MotionState a = new MotionState(0.0, 0.0, 1.0, 0.0);

        MotionState expected_after_0s = new MotionState(0.0, 0.0, 1.0, 0.0);
        assertThat(a.extrapolate(0.0), is(equalTo(expected_after_0s)));

        MotionState expected_after_1s = new MotionState(1.0, 1.0, 1.0, 0.0);
        assertThat(a.extrapolate(1.0), is(equalTo(expected_after_1s)));
        assertThat(expected_after_1s.extrapolate(0.0), is(equalTo(a)));

        MotionState expected_after_neg1s = new MotionState(-1.0, -1.0, 1.0, 0.0);
        assertThat(a.extrapolate(-1.0), is(equalTo(expected_after_neg1s)));
        assertThat(expected_after_neg1s.extrapolate(0.0), is(equalTo(a)));
    }

    @Test
    public void extrapolateConstantAccel() {
        // vel = 1, acc = 1
        MotionState a = new MotionState(0.0, 0.0, 1.0, 1.0);

        MotionState expected_after_0s = new MotionState(0.0, 0.0, 1.0, 1.0);
        assertThat(a.extrapolate(0.0), is(equalTo(expected_after_0s)));

        MotionState expected_after_1s = new MotionState(1.0, 1.5, 2.0, 1.0);
        assertThat(a.extrapolate(1.0), is(equalTo(expected_after_1s)));
        assertThat(expected_after_1s.extrapolate(0.0), is(equalTo(a)));

        MotionState expected_after_neg1s = new MotionState(-1.0, -0.5, 0.0, 1.0);
        assertThat(a.extrapolate(-1.0), is(equalTo(expected_after_neg1s)));
        assertThat(expected_after_neg1s.extrapolate(0.0), is(equalTo(a)));
    }

    @Test
    public void nextTimeAtPosStationary() {
        MotionState a = new MotionState(0.0, 3.0, 0.0, 0.0);
        assertThat(a.nextTimeAtPos(3.0), is(equalTo(0.0)));

        a = new MotionState(1.0, 3.0, 0.0, 0.0);
        assertThat(a.nextTimeAtPos(3.0), is(equalTo(1.0)));
    }

    @Test
    public void nextTimeAtPosConstantVelocity() {
        MotionState a = new MotionState(0.0, 1.0, 1.0, 0.0);
        assertThat(a.nextTimeAtPos(1.0), is(equalTo(0.0)));
        assertThat(a.nextTimeAtPos(2.0), is(equalTo(1.0)));
        assertThat(a.nextTimeAtPos(0.0), is(equalTo(Double.NaN)));

        a = new MotionState(1.0, 1.0, 1.0, 0.0);
        assertThat(a.nextTimeAtPos(1.0), is(equalTo(1.0)));
        assertThat(a.nextTimeAtPos(2.0), is(equalTo(2.0)));
        assertThat(a.nextTimeAtPos(0.0), is(equalTo(Double.NaN)));
    }

    @Test
    public void nextTimeAtPosConstantAccel() {
        MotionState a = new MotionState(0.0, 0.0, 0.0, 1.0);
        assertThat(a.nextTimeAtPos(0.0), is(equalTo(0.0)));
        assertThat(a.nextTimeAtPos(0.5), is(equalTo(1.0)));
        assertThat(a.nextTimeAtPos(-1.0), is(equalTo(Double.NaN)));

        a = new MotionState(1.0, 1.0, 1.0, 1.0);
        assertThat(a.nextTimeAtPos(1.0), is(equalTo(1.0)));
        assertThat(a.nextTimeAtPos(2.5), is(equalTo(2.0)));
        assertThat(a.nextTimeAtPos(-1.0), is(equalTo(Double.NaN)));

        a = new MotionState(1.0, 1.0, 1.0, -1.0);
        assertThat(a.nextTimeAtPos(1.0), is(equalTo(1.0)));
        assertThat(a.nextTimeAtPos(1.5), is(equalTo(2.0)));
        assertThat(a.nextTimeAtPos(1.51), is(equalTo(Double.NaN)));
        assertThat(a.nextTimeAtPos(-0.5), is(equalTo(4.0)));
    }

    @Test
    public void flipped() {
        MotionState a = new MotionState(1.0, 1.0, 1.0, 1.0);
        assertThat(a.flipped(), is(equalTo(new MotionState(1.0, -1.0, -1.0, -1.0))));
        assertThat(a.flipped().flipped(), is(equalTo(a)));
    }
}
