package com.team254.lib.util.control;

import static org.junit.Assert.*;

import com.team254.lib.util.control.PathSegment;
import com.team254.lib.util.math.Translation2d;
import com.team254.lib.util.motion.MotionState;

import org.junit.Test;

public class PathTest {
    public static final double kTestEpsilon = 1E-9;
    public static final MotionState stopped = new MotionState(0, 0, 0, 0);

    @Test
    public void testLinearPathSegment() {
        PathSegment segment = new PathSegment(0.0, 0.0, 100.0, 0.0, 50.0, stopped, 0.0);
        assertEquals(100, segment.getLength(), kTestEpsilon);

        // GetClosestPoint - point on path
        Translation2d closestPoint = segment.getClosestPoint(new Translation2d(50, 0));
        assertEquals(50, closestPoint.x(), kTestEpsilon);
        assertEquals(0, closestPoint.y(), kTestEpsilon);
        double dist = segment.getRemainingDistance(closestPoint);
        assertEquals(50, dist, kTestEpsilon);
        Translation2d lookAheadPoint = segment.getPointByDistance(75.0);
        assertEquals(75, lookAheadPoint.x(), kTestEpsilon);
        assertEquals(0, lookAheadPoint.y(), kTestEpsilon);

        // GetClosestPoint - point off of path
        closestPoint = segment.getClosestPoint(new Translation2d(20, 50));
        assertEquals(20, closestPoint.x(), kTestEpsilon);
        assertEquals(0, closestPoint.y(), kTestEpsilon);
        dist = segment.getRemainingDistance(closestPoint);
        assertEquals(80, dist, kTestEpsilon);

        // GetClosestPoint - point behind start
        closestPoint = segment.getClosestPoint(new Translation2d(-30, -30));
        assertEquals(0, closestPoint.x(), kTestEpsilon);
        assertEquals(0, closestPoint.y(), kTestEpsilon);
        dist = segment.getRemainingDistance(closestPoint);
        assertEquals(100, dist, kTestEpsilon);

        // GetClosestPoint - point after end
        closestPoint = segment.getClosestPoint(new Translation2d(120, 150));
        assertEquals(100, closestPoint.x(), kTestEpsilon);
        assertEquals(0, closestPoint.y(), kTestEpsilon);
        dist = segment.getRemainingDistance(closestPoint);
        assertEquals(0, dist, kTestEpsilon);

        // Try a different linear segment
        segment = new PathSegment(10.0, -12.0, -30.0, -120.0, 100, stopped, 0.0);
        assertEquals(115.169440391, segment.getLength(), kTestEpsilon);

        // GetClosestPoint - point on path
        closestPoint = segment.getClosestPoint(new Translation2d(-20, -93));
        assertEquals(-20, closestPoint.x(), kTestEpsilon);
        assertEquals(-93, closestPoint.y(), kTestEpsilon);
        dist = segment.getRemainingDistance(closestPoint);
        assertEquals(28.7923600978, dist, kTestEpsilon);
        lookAheadPoint = segment.getPointByDistance(75.0);
        assertEquals(-16.048576686769547, lookAheadPoint.x(), kTestEpsilon);
        assertEquals(-82.33115705427778, lookAheadPoint.y(), kTestEpsilon);

        // GetClosestPoint - point off of path
        closestPoint = segment.getClosestPoint(new Translation2d(30, -39));
        assertEquals(3.618817852834706, closestPoint.x(), kTestEpsilon);
        assertEquals(-29.2291917973462, closestPoint.y(), kTestEpsilon);
        dist = segment.getRemainingDistance(closestPoint);
        assertEquals(96.79651096803562, dist, kTestEpsilon);

        // GetClosestPoint - point behind start
        closestPoint = segment.getClosestPoint(new Translation2d(30, 30));
        assertEquals(10, closestPoint.x(), kTestEpsilon);
        assertEquals(-12, closestPoint.y(), kTestEpsilon);
        dist = segment.getRemainingDistance(closestPoint);
        assertEquals(115.169440391, dist, kTestEpsilon);

        // GetClosestPoint - point after end
        closestPoint = segment.getClosestPoint(new Translation2d(-21, -150));
        assertEquals(-30, closestPoint.x(), kTestEpsilon);
        assertEquals(-120, closestPoint.y(), kTestEpsilon);
        dist = segment.getRemainingDistance(closestPoint);
        assertEquals(0, dist, kTestEpsilon);
    }

    public void testArcPathSegment() {
        PathSegment segment = new PathSegment(0.0, 0.0, 100.0, 0.0, 50.0, stopped, 0.0);
        assertEquals(100, segment.getLength(), kTestEpsilon);

        // GetClosestPoint - point on path
        Translation2d closestPoint = segment.getClosestPoint(new Translation2d(50, 0));
        assertEquals(50, closestPoint.x(), kTestEpsilon);
        assertEquals(0, closestPoint.y(), kTestEpsilon);
        double dist = segment.getRemainingDistance(closestPoint);
        assertEquals(50, dist, kTestEpsilon);
        Translation2d lookAheadPoint = segment.getPointByDistance(75.0);
        assertEquals(75, lookAheadPoint.x(), kTestEpsilon);
        assertEquals(0, lookAheadPoint.y(), kTestEpsilon);

        // GetClosestPoint - point off of path
        closestPoint = segment.getClosestPoint(new Translation2d(20, 50));
        assertEquals(20, closestPoint.x(), kTestEpsilon);
        assertEquals(0, closestPoint.y(), kTestEpsilon);
        dist = segment.getRemainingDistance(closestPoint);
        assertEquals(80, dist, kTestEpsilon);

        // GetClosestPoint - point behind start
        closestPoint = segment.getClosestPoint(new Translation2d(-30, -30));
        assertEquals(0, closestPoint.x(), kTestEpsilon);
        assertEquals(0, closestPoint.y(), kTestEpsilon);
        dist = segment.getRemainingDistance(closestPoint);
        assertEquals(100, dist, kTestEpsilon);

        // GetClosestPoint - point after end
        closestPoint = segment.getClosestPoint(new Translation2d(120, 150));
        assertEquals(100, closestPoint.x(), kTestEpsilon);
        assertEquals(0, closestPoint.y(), kTestEpsilon);
        dist = segment.getRemainingDistance(closestPoint);
        assertEquals(0, dist, kTestEpsilon);
    }
}