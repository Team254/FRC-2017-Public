package com.team254.lib.util.control;

import static org.junit.Assert.*;

import org.junit.Test;

import com.team254.lib.util.control.AdaptivePurePursuitController;
import com.team254.lib.util.control.AdaptivePurePursuitController.Arc;
import com.team254.lib.util.math.RigidTransform2d;
import com.team254.lib.util.math.Rotation2d;
import com.team254.lib.util.math.Translation2d;

public class AdaptivePurePursuitControllerTest {
    private static final double kEpsilon = 1E-6;
    private static final double kReallyBigNumber = 1E6;

    @Test
    public void testJoinRadius() {
        // Robot is at the origin, facing positive x
        RigidTransform2d robot_pose = new RigidTransform2d();

        // Lookahead point is straight ahead
        Translation2d lookahead_point = new Translation2d(30, 0);
        // Radius should be yuuuuge
        assertTrue(AdaptivePurePursuitController.getRadius(robot_pose, lookahead_point) > kReallyBigNumber);

        robot_pose.setRotation(Rotation2d.fromDegrees(45));
        lookahead_point = new Translation2d(30, 30);
        assertTrue(AdaptivePurePursuitController.getRadius(robot_pose, lookahead_point) > kReallyBigNumber);

        robot_pose.setRotation(Rotation2d.fromDegrees(-45));
        lookahead_point = new Translation2d(30, -30);
        assertTrue(AdaptivePurePursuitController.getRadius(robot_pose, lookahead_point) > kReallyBigNumber);

        robot_pose.setRotation(Rotation2d.fromDegrees(180));
        lookahead_point = new Translation2d(-30, 0);
        assertTrue(AdaptivePurePursuitController.getRadius(robot_pose, lookahead_point) > kReallyBigNumber);

        robot_pose.setRotation(Rotation2d.fromDegrees(-20));
        lookahead_point = new Translation2d(40, 10);
        assertEquals(AdaptivePurePursuitController.getRadius(robot_pose, lookahead_point), 36.83204234182525, kEpsilon);

        robot_pose.setRotation(Rotation2d.fromDegrees(-130));
        lookahead_point = new Translation2d(-40, 10);
        System.out.println(AdaptivePurePursuitController.getRadius(robot_pose, lookahead_point));
        assertEquals(AdaptivePurePursuitController.getRadius(robot_pose, lookahead_point), 22.929806792642722,
                kEpsilon);
    }

    @Test
    public void testArc() {
        // Robot is at the origin, facing positive x
        RigidTransform2d robot_pose = new RigidTransform2d();
        // Lookahead point is straight ahead
        Translation2d lookahead_point = new Translation2d(30, 0);
        Arc arc = new Arc(robot_pose, lookahead_point);
        assertTrue(arc.radius > kReallyBigNumber);
        assertEquals(arc.length, 30.0, kEpsilon);

        // Lookahead point is ahead and to the left
        lookahead_point = new Translation2d(30, 30);
        arc = new Arc(robot_pose, lookahead_point);
        assertEquals(arc.radius, 30.0, kEpsilon);
        assertEquals(arc.length, 30.0 * Math.PI / 2, kEpsilon);
        assertEquals(arc.center.x(), 0.0, kEpsilon);
        assertEquals(arc.center.y(), 30.0, kEpsilon);

        // Lookahead point is ahead and to the right
        lookahead_point = new Translation2d(30, -30);
        arc = new Arc(robot_pose, lookahead_point);
        assertEquals(arc.radius, 30.0, kEpsilon);
        assertEquals(arc.length, 30.0 * Math.PI / 2, kEpsilon);
        assertEquals(arc.center.x(), 0.0, kEpsilon);
        assertEquals(arc.center.y(), -30.0, kEpsilon);

        // Lookahead point is behind and to the right
        lookahead_point = new Translation2d(-30, -30);
        arc = new Arc(robot_pose, lookahead_point);
        assertEquals(arc.radius, 30.0, kEpsilon);
        assertEquals(arc.length, 3 * 30.0 * Math.PI / 2, kEpsilon);
        assertEquals(arc.center.x(), 0.0, kEpsilon);
        assertEquals(arc.center.y(), -30.0, kEpsilon);

        // Robot is rotated, point is directly ahead.
        robot_pose = new RigidTransform2d(new Translation2d(3.0, 4.0), Rotation2d.fromDegrees(45.0));
        lookahead_point = new Translation2d(33, 34);
        arc = new Arc(robot_pose, lookahead_point);
        assertTrue(arc.radius > kReallyBigNumber);
        assertEquals(arc.length, Math.hypot(30.0, 30.0), kEpsilon);

        // Robot is rotated, point is directly to our left.
        robot_pose = new RigidTransform2d(new Translation2d(3.0, 4.0), Rotation2d.fromDegrees(-45.0));
        lookahead_point = new Translation2d(33, 34);
        arc = new Arc(robot_pose, lookahead_point);
        assertEquals(arc.radius, Math.hypot(30.0, 30.0) / 2, kEpsilon);
        assertEquals(arc.length, Math.hypot(30.0, 30.0) / 2 * Math.PI, kEpsilon);
        assertEquals(arc.center.x(), 18.0, kEpsilon);
        assertEquals(arc.center.y(), 19.0, kEpsilon);

        // Lookahead point is at the robot
        robot_pose = new RigidTransform2d(new Translation2d(3.0, 4.0), Rotation2d.fromDegrees(-45.0));
        lookahead_point = new Translation2d(3, 4);
        arc = new Arc(robot_pose, lookahead_point);
        assertEquals(arc.radius, 0, kEpsilon);
        assertEquals(arc.length, 0, kEpsilon);
        assertEquals(arc.center.x(), 3.0, kEpsilon);
        assertEquals(arc.center.y(), 4.0, kEpsilon);
    }
}
