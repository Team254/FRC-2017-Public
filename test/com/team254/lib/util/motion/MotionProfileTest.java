package com.team254.lib.util.motion;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;

public class MotionProfileTest {
    List<MotionSegment> segments;
    List<MotionSegment> segments_reversed;
    List<MotionSegment> segments_combined;

    @Before
    public void setUp() {
        segments = new ArrayList<>();
        // Accelerate at 1 unit/s^2 for 1 second
        segments.add(new MotionSegment(new MotionState(0.0, 0.0, 0.0, 1.0), new MotionState(1.0, 0.5, 1.0, 1.0)));
        // Cruise at 1 unit/s for 1 second
        segments.add(new MotionSegment(new MotionState(1.0, 0.5, 1.0, 0.0), new MotionState(2.0, 1.5, 1.0, 0.0)));
        // Decelerate at -1 unit/s^2 for 1 second
        segments.add(new MotionSegment(new MotionState(2.0, 1.5, 1.0, -1.0), new MotionState(3.0, 2.0, 0.0, -1.0)));

        segments_reversed = new ArrayList<>();
        // Accelerate at -1 unit/s^2 for 1 second
        segments_reversed
                .add(new MotionSegment(new MotionState(3.0, 2.0, 0.0, -1.0), new MotionState(4.0, 1.5, -1.0, -1.0)));
        // Cruise at -1 unit/s for 1 second
        segments_reversed
                .add(new MotionSegment(new MotionState(4.0, 1.5, -1.0, 0.0), new MotionState(5.0, 0.5, -1.0, 0.0)));
        // Decelerate at -1 unit/s^2 for 1 second
        segments_reversed
                .add(new MotionSegment(new MotionState(5.0, 0.5, -1.0, 1.0), new MotionState(6.0, 0.0, 0.0, 1.0)));

        segments_combined = new ArrayList<>();
        segments_combined.addAll(segments);
        segments_combined.addAll(segments_reversed);
    }

    @Test
    public void smoke() {
        MotionProfile a = new MotionProfile();
        assertTrue(a.isEmpty());
        assertTrue(a.isValid());
        assertThat(a.length(), is(equalTo(0.0)));
        assertThat(a.duration(), is(equalTo(Double.NaN)));

        a = new MotionProfile(segments);
        assertFalse(a.isEmpty());
        assertTrue(a.isValid());
        assertThat(a.startPos(), is(equalTo(0.0)));
        assertThat(a.endPos(), is(equalTo(2.0)));
        assertThat(a.length(), is(equalTo(2.0)));
        assertThat(a.startTime(), is(equalTo(0.0)));
        assertThat(a.endTime(), is(equalTo(3.0)));
        assertThat(a.duration(), is(equalTo(3.0)));
    }

    @Test
    public void isValid() {
        MotionProfile a = new MotionProfile();
        a.appendSegment(new MotionSegment(new MotionState(0.0, 0.0, 0.0, 0.0), new MotionState(0.0, 0.0, 0.0, 1.0)));
        assertFalse(a.isValid());

        a = new MotionProfile();
        a.appendSegment(new MotionSegment(new MotionState(0.0, 0.0, 0.0, 1.0), new MotionState(1.0, 0.0, 1.0, 1.0)));
        assertFalse(a.isValid());

        a = new MotionProfile();
        a.appendSegment(new MotionSegment(new MotionState(0.0, 0.0, 0.0, 1.0), new MotionState(1.0, .5, 1.0, 1.0)));
        assertTrue(a.isValid());
    }

    @Test
    public void appendControl() {
        MotionProfile a = new MotionProfile(segments);

        // 3 second trapezoid traveling a displacement of 2 units. Peak velocity of 1.0 units/s.
        a.appendControl(1.0, 1.0);
        a.appendControl(0.0, 1.0);
        a.appendControl(-1.0, 1.0);
        assertFalse(a.isEmpty());
        assertTrue(a.isValid());
        assertThat(a.startPos(), is(equalTo(0.0)));
        assertThat(a.endPos(), is(equalTo(4.0)));
        assertThat(a.length(), is(equalTo(4.0)));
        assertThat(a.startTime(), is(equalTo(0.0)));
        assertThat(a.endTime(), is(equalTo(6.0)));
        assertThat(a.duration(), is(equalTo(6.0)));
    }

    @Test
    public void appendSegment() {
        MotionProfile a = new MotionProfile(segments);

        // 3 second trapezoid traveling a displacement of 2 units. Peak velocity of 1.0 units/s.
        a.appendSegment(new MotionSegment(new MotionState(3.0, 2.0, 0.0, 1.0), new MotionState(4.0, 2.5, 1.0, 1.0)));
        a.appendSegment(new MotionSegment(new MotionState(4.0, 2.5, 1.0, 0.0), new MotionState(5.0, 3.5, 1.0, 0.0)));
        a.appendSegment(new MotionSegment(new MotionState(5.0, 3.5, 1.0, -1.0), new MotionState(6.0, 4.0, 0.0, -1.0)));
        assertFalse(a.isEmpty());
        assertTrue(a.isValid());
        assertThat(a.startPos(), is(equalTo(0.0)));
        assertThat(a.endPos(), is(equalTo(4.0)));
        assertThat(a.length(), is(equalTo(4.0)));
        assertThat(a.startTime(), is(equalTo(0.0)));
        assertThat(a.endTime(), is(equalTo(6.0)));
        assertThat(a.duration(), is(equalTo(6.0)));

        // Add an invalid segment.
        a.appendSegment(new MotionSegment(new MotionState(3.0, 2.0, 0.0, 1.0), new MotionState(4.0, 2.5, 1.0, 1.0)));
        assertFalse(a.isValid());
    }

    @Test
    public void appendProfile() {
        MotionProfile a = new MotionProfile(segments);
        MotionProfile b = new MotionProfile(segments_reversed);
        a.appendProfile(b);
        assertTrue(a.isValid());
        assertThat(a.endState(), is(equalTo(b.endState())));
    }

    @Test
    public void stateByTime() {
        MotionProfile a = new MotionProfile(segments);
        assertThat(a.stateByTime(-0.1), is(equalTo(Optional.empty())));
        assertThat(a.stateByTime(0.0).get(), is(equalTo(segments.get(0).start())));
        assertThat(a.stateByTime(0.5).get(), is(equalTo(segments.get(0).start().extrapolate(0.5))));
        assertThat(a.stateByTime(1.0).get(), is(equalTo(segments.get(0).end())));
        assertThat(a.stateByTime(1.25).get(), is(equalTo(segments.get(1).start().extrapolate(1.25))));
        assertThat(a.stateByTime(2.0).get(), is(equalTo(segments.get(1).end())));
        assertThat(a.stateByTime(3.0).get(), is(equalTo(segments.get(2).end())));
        assertThat(a.stateByTime(3.1), is(equalTo(Optional.empty())));

        a = new MotionProfile(segments_reversed);
        assertThat(a.stateByTime(2.9), is(equalTo(Optional.empty())));
        assertThat(a.stateByTime(3.0).get(), is(equalTo(segments_reversed.get(0).start())));
        assertThat(a.stateByTime(3.5).get(), is(equalTo(segments_reversed.get(0).start().extrapolate(3.5))));
        assertThat(a.stateByTime(4.0).get(), is(equalTo(segments_reversed.get(0).end())));
        assertThat(a.stateByTime(4.25).get(), is(equalTo(segments_reversed.get(1).start().extrapolate(4.25))));
        assertThat(a.stateByTime(5.0).get(), is(equalTo(segments_reversed.get(1).end())));
        assertThat(a.stateByTime(6.0).get(), is(equalTo(segments_reversed.get(2).end())));
        assertThat(a.stateByTime(6.1), is(equalTo(Optional.empty())));
    }

    @Test
    public void firstStateByPos() {
        MotionProfile a = new MotionProfile(segments);
        assertThat(a.firstStateByPos(-0.1), is(equalTo(Optional.empty())));
        assertThat(a.firstStateByPos(0.0).get(), is(equalTo(segments.get(0).start())));
        assertThat(a.firstStateByPos(0.5).get(), is(equalTo(segments.get(0).end())));
        assertThat(a.firstStateByPos(1.0).get(), is(equalTo(segments.get(1).start().extrapolate(1.5))));
        assertThat(a.firstStateByPos(1.5).get(), is(equalTo(segments.get(1).end())));
        assertThat(a.firstStateByPos(2.0).get(), is(equalTo(segments.get(2).end())));
        assertThat(a.firstStateByPos(2.1), is(equalTo(Optional.empty())));

        a = new MotionProfile(segments_reversed);
        assertThat(a.firstStateByPos(-0.1), is(equalTo(Optional.empty())));
        assertThat(a.firstStateByPos(0.0).get(), is(equalTo(segments_reversed.get(2).end())));
        assertThat(a.firstStateByPos(0.5).get(), is(equalTo(segments_reversed.get(1).end())));
        assertThat(a.firstStateByPos(1.0).get(), is(equalTo(segments_reversed.get(1).start().extrapolate(4.5))));
        assertThat(a.firstStateByPos(1.5).get(), is(equalTo(segments_reversed.get(0).end())));
        assertThat(a.firstStateByPos(2.0).get(), is(equalTo(segments_reversed.get(0).start())));
        assertThat(a.firstStateByPos(2.1), is(equalTo(Optional.empty())));

        a = new MotionProfile(segments_combined);
        assertThat(a.firstStateByPos(-0.1), is(equalTo(Optional.empty())));
        assertThat(a.firstStateByPos(0.0).get(), is(equalTo(segments.get(0).start())));
        assertThat(a.firstStateByPos(0.5).get(), is(equalTo(segments.get(0).end())));
        assertThat(a.firstStateByPos(1.0).get(), is(equalTo(segments.get(1).start().extrapolate(1.5))));
        assertThat(a.firstStateByPos(1.5).get(), is(equalTo(segments.get(1).end())));
        assertThat(a.firstStateByPos(2.0).get(), is(equalTo(segments.get(2).end())));
        assertThat(a.firstStateByPos(2.1), is(equalTo(Optional.empty())));
    }

    @Test
    public void trimBeforeTime() {
        List<MotionSegment> segments_copy = new ArrayList<>();
        segments_copy.addAll(segments);
        MotionProfile a = new MotionProfile(segments_copy);
        a.trimBeforeTime(-1.0);
        assertThat(a.stateByTime(0.0).get(), is(equalTo(segments.get(0).start())));
        a.trimBeforeTime(0.0);
        assertThat(a.stateByTime(0.0).get(), is(equalTo(segments.get(0).start())));
        a.trimBeforeTime(0.5);
        assertThat(a.stateByTime(0.0), is(equalTo(Optional.empty())));
        assertThat(a.stateByTime(0.5).get(), is(equalTo(segments.get(0).start().extrapolate(0.5))));
        a.trimBeforeTime(1.0);
        assertThat(a.stateByTime(0.5), is(equalTo(Optional.empty())));
        assertThat(a.stateByTime(1.0).get(), is(equalTo(segments.get(1).start())));
        a.trimBeforeTime(10.0);
        assertTrue(a.isEmpty());
    }
}
