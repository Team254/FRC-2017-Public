package com.team254.lib.util.math;

import static org.junit.Assert.*;

import org.junit.Test;

import com.team254.frc2017.Constants;
import com.team254.lib.util.math.RigidTransform2d;
import com.team254.lib.util.math.Rotation2d;
import com.team254.lib.util.math.Translation2d;
import com.team254.lib.util.math.Twist2d;

public class TestMath {
    public static final double kTestEpsilon = 1E-9;

    @Test
    public void testRotation2d() {
        // Test constructors
        Rotation2d rot1 = new Rotation2d();
        assertEquals(1, rot1.cos(), kTestEpsilon);
        assertEquals(0, rot1.sin(), kTestEpsilon);
        assertEquals(0, rot1.tan(), kTestEpsilon);
        assertEquals(0, rot1.getDegrees(), kTestEpsilon);
        assertEquals(0, rot1.getRadians(), kTestEpsilon);

        rot1 = new Rotation2d(1, 1, true);
        assertEquals(Math.sqrt(2) / 2, rot1.cos(), kTestEpsilon);
        assertEquals(Math.sqrt(2) / 2, rot1.sin(), kTestEpsilon);
        assertEquals(1, rot1.tan(), kTestEpsilon);
        assertEquals(45, rot1.getDegrees(), kTestEpsilon);
        assertEquals(Math.PI / 4, rot1.getRadians(), kTestEpsilon);

        rot1 = Rotation2d.fromRadians(Math.PI / 2);
        assertEquals(0, rot1.cos(), kTestEpsilon);
        assertEquals(1, rot1.sin(), kTestEpsilon);
        assertTrue(1 / kTestEpsilon < rot1.tan());
        assertEquals(90, rot1.getDegrees(), kTestEpsilon);
        assertEquals(Math.PI / 2, rot1.getRadians(), kTestEpsilon);

        rot1 = Rotation2d.fromDegrees(270);
        assertEquals(0, rot1.cos(), kTestEpsilon);
        assertEquals(-1, rot1.sin(), kTestEpsilon);
        System.out.println(rot1.tan());
        assertTrue(-1 / kTestEpsilon > rot1.tan());
        assertEquals(-90, rot1.getDegrees(), kTestEpsilon);
        assertEquals(-Math.PI / 2, rot1.getRadians(), kTestEpsilon);

        // Test inversion
        rot1 = Rotation2d.fromDegrees(270);
        Rotation2d rot2 = rot1.inverse();
        assertEquals(0, rot2.cos(), kTestEpsilon);
        assertEquals(1, rot2.sin(), kTestEpsilon);
        assertTrue(1 / kTestEpsilon < rot2.tan());
        assertEquals(90, rot2.getDegrees(), kTestEpsilon);
        assertEquals(Math.PI / 2, rot2.getRadians(), kTestEpsilon);

        rot1 = Rotation2d.fromDegrees(1);
        rot2 = rot1.inverse();
        assertEquals(rot1.cos(), rot2.cos(), kTestEpsilon);
        assertEquals(-rot1.sin(), rot2.sin(), kTestEpsilon);
        assertEquals(-1, rot2.getDegrees(), kTestEpsilon);

        // Test rotateBy
        rot1 = Rotation2d.fromDegrees(45);
        rot2 = Rotation2d.fromDegrees(45);
        Rotation2d rot3 = rot1.rotateBy(rot2);
        assertEquals(0, rot3.cos(), kTestEpsilon);
        assertEquals(1, rot3.sin(), kTestEpsilon);
        assertTrue(1 / kTestEpsilon < rot3.tan());
        assertEquals(90, rot3.getDegrees(), kTestEpsilon);
        assertEquals(Math.PI / 2, rot3.getRadians(), kTestEpsilon);

        rot1 = Rotation2d.fromDegrees(45);
        rot2 = Rotation2d.fromDegrees(-45);
        rot3 = rot1.rotateBy(rot2);
        assertEquals(1, rot3.cos(), kTestEpsilon);
        assertEquals(0, rot3.sin(), kTestEpsilon);
        assertEquals(0, rot3.tan(), kTestEpsilon);
        assertEquals(0, rot3.getDegrees(), kTestEpsilon);
        assertEquals(0, rot3.getRadians(), kTestEpsilon);

        // A rotation times its inverse should be the identity
        Rotation2d identity = new Rotation2d();
        rot1 = Rotation2d.fromDegrees(21.45);
        rot2 = rot1.rotateBy(rot1.inverse());
        assertEquals(identity.cos(), rot2.cos(), kTestEpsilon);
        assertEquals(identity.sin(), rot2.sin(), kTestEpsilon);
        assertEquals(identity.getDegrees(), rot2.getDegrees(), kTestEpsilon);

        // Test interpolation
        rot1 = Rotation2d.fromDegrees(45);
        rot2 = Rotation2d.fromDegrees(135);
        rot3 = rot1.interpolate(rot2, .5);
        assertEquals(90, rot3.getDegrees(), kTestEpsilon);

        rot1 = Rotation2d.fromDegrees(45);
        rot2 = Rotation2d.fromDegrees(135);
        rot3 = rot1.interpolate(rot2, .75);
        assertEquals(112.5, rot3.getDegrees(), kTestEpsilon);

        rot1 = Rotation2d.fromDegrees(45);
        rot2 = Rotation2d.fromDegrees(-45);
        rot3 = rot1.interpolate(rot2, .5);
        assertEquals(0, rot3.getDegrees(), kTestEpsilon);

        rot1 = Rotation2d.fromDegrees(45);
        rot2 = Rotation2d.fromDegrees(45);
        rot3 = rot1.interpolate(rot2, .5);
        assertEquals(45, rot3.getDegrees(), kTestEpsilon);

        rot1 = Rotation2d.fromDegrees(45);
        rot2 = Rotation2d.fromDegrees(45);
        rot3 = rot1.interpolate(rot2, .5);
        assertEquals(45, rot3.getDegrees(), kTestEpsilon);

        // Test parallel.
        rot1 = Rotation2d.fromDegrees(45);
        rot2 = Rotation2d.fromDegrees(45);
        assertTrue(rot1.isParallel(rot2));

        rot1 = Rotation2d.fromDegrees(45);
        rot2 = Rotation2d.fromDegrees(-45);
        assertFalse(rot1.isParallel(rot2));

        rot1 = Rotation2d.fromDegrees(45);
        rot2 = Rotation2d.fromDegrees(-135);
        assertTrue(rot1.isParallel(rot2));
    }

    @Test
    public void testTranslation2d() {
        // Test constructors
        Translation2d pos1 = new Translation2d();
        assertEquals(0, pos1.x(), kTestEpsilon);
        assertEquals(0, pos1.y(), kTestEpsilon);
        assertEquals(0, pos1.norm(), kTestEpsilon);

        pos1.setX(3);
        pos1.setY(4);
        assertEquals(3, pos1.x(), kTestEpsilon);
        assertEquals(4, pos1.y(), kTestEpsilon);
        assertEquals(5, pos1.norm(), kTestEpsilon);

        pos1 = new Translation2d(3, 4);
        assertEquals(3, pos1.x(), kTestEpsilon);
        assertEquals(4, pos1.y(), kTestEpsilon);
        assertEquals(5, pos1.norm(), kTestEpsilon);

        // Test inversion
        pos1 = new Translation2d(3.152, 4.1666);
        Translation2d pos2 = pos1.inverse();
        assertEquals(-pos1.x(), pos2.x(), kTestEpsilon);
        assertEquals(-pos1.y(), pos2.y(), kTestEpsilon);
        assertEquals(pos1.norm(), pos2.norm(), kTestEpsilon);

        // Test rotateBy
        pos1 = new Translation2d(2, 0);
        Rotation2d rot1 = Rotation2d.fromDegrees(90);
        pos2 = pos1.rotateBy(rot1);
        assertEquals(0, pos2.x(), kTestEpsilon);
        assertEquals(2, pos2.y(), kTestEpsilon);
        assertEquals(pos1.norm(), pos2.norm(), kTestEpsilon);

        pos1 = new Translation2d(2, 0);
        rot1 = Rotation2d.fromDegrees(-45);
        pos2 = pos1.rotateBy(rot1);
        assertEquals(Math.sqrt(2), pos2.x(), kTestEpsilon);
        assertEquals(-Math.sqrt(2), pos2.y(), kTestEpsilon);
        assertEquals(pos1.norm(), pos2.norm(), kTestEpsilon);

        // Test translateBy
        pos1 = new Translation2d(2, 0);
        pos2 = new Translation2d(-2, 1);
        Translation2d pos3 = pos1.translateBy(pos2);
        assertEquals(0, pos3.x(), kTestEpsilon);
        assertEquals(1, pos3.y(), kTestEpsilon);
        assertEquals(1, pos3.norm(), kTestEpsilon);

        // A translation times its inverse should be the identity
        Translation2d identity = new Translation2d();
        pos1 = new Translation2d(2.16612, -23.55);
        pos2 = pos1.translateBy(pos1.inverse());
        assertEquals(identity.x(), pos2.x(), kTestEpsilon);
        assertEquals(identity.y(), pos2.y(), kTestEpsilon);
        assertEquals(identity.norm(), pos2.norm(), kTestEpsilon);

        // Test interpolation
        pos1 = new Translation2d(0, 1);
        pos2 = new Translation2d(10, -1);
        pos3 = pos1.interpolate(pos2, .5);
        assertEquals(5, pos3.x(), kTestEpsilon);
        assertEquals(0, pos3.y(), kTestEpsilon);

        pos1 = new Translation2d(0, 1);
        pos2 = new Translation2d(10, -1);
        pos3 = pos1.interpolate(pos2, .75);
        assertEquals(7.5, pos3.x(), kTestEpsilon);
        assertEquals(-.5, pos3.y(), kTestEpsilon);
    }

    @Test
    public void testRigidTransform2d() {
        // Test constructors
        RigidTransform2d pose1 = new RigidTransform2d();
        assertEquals(0, pose1.getTranslation().x(), kTestEpsilon);
        assertEquals(0, pose1.getTranslation().y(), kTestEpsilon);
        assertEquals(0, pose1.getRotation().getDegrees(), kTestEpsilon);

        pose1 = new RigidTransform2d(new Translation2d(3, 4), Rotation2d.fromDegrees(45));
        assertEquals(3, pose1.getTranslation().x(), kTestEpsilon);
        assertEquals(4, pose1.getTranslation().y(), kTestEpsilon);
        assertEquals(45, pose1.getRotation().getDegrees(), kTestEpsilon);

        // Test transformation
        pose1 = new RigidTransform2d(new Translation2d(3, 4), Rotation2d.fromDegrees(90));
        RigidTransform2d pose2 = new RigidTransform2d(new Translation2d(1, 0), Rotation2d.fromDegrees(0));
        RigidTransform2d pose3 = pose1.transformBy(pose2);
        assertEquals(3, pose3.getTranslation().x(), kTestEpsilon);
        assertEquals(5, pose3.getTranslation().y(), kTestEpsilon);
        assertEquals(90, pose3.getRotation().getDegrees(), kTestEpsilon);

        pose1 = new RigidTransform2d(new Translation2d(3, 4), Rotation2d.fromDegrees(90));
        pose2 = new RigidTransform2d(new Translation2d(1, 0), Rotation2d.fromDegrees(-90));
        pose3 = pose1.transformBy(pose2);
        assertEquals(3, pose3.getTranslation().x(), kTestEpsilon);
        assertEquals(5, pose3.getTranslation().y(), kTestEpsilon);
        assertEquals(0, pose3.getRotation().getDegrees(), kTestEpsilon);

        // A pose times its inverse should be the identity
        RigidTransform2d identity = new RigidTransform2d();
        pose1 = new RigidTransform2d(new Translation2d(3.51512152, 4.23), Rotation2d.fromDegrees(91.6));
        pose2 = pose1.transformBy(pose1.inverse());
        assertEquals(identity.getTranslation().x(), pose2.getTranslation().x(), kTestEpsilon);
        assertEquals(identity.getTranslation().y(), pose2.getTranslation().y(), kTestEpsilon);
        assertEquals(identity.getRotation().getDegrees(), pose2.getRotation().getDegrees(), kTestEpsilon);

        // Test interpolation
        // Movement from pose1 to pose2 is along a circle with radius of 10 units centered at (3, -6)
        pose1 = new RigidTransform2d(new Translation2d(3, 4), Rotation2d.fromDegrees(90));
        pose2 = new RigidTransform2d(new Translation2d(13, -6), Rotation2d.fromDegrees(0.0));
        pose3 = pose1.interpolate(pose2, .5);
        double expected_angle_rads = Math.PI / 4;
        assertEquals(3.0 + 10.0 * Math.cos(expected_angle_rads), pose3.getTranslation().x(), kTestEpsilon);
        assertEquals(-6.0 + 10.0 * Math.sin(expected_angle_rads), pose3.getTranslation().y(), kTestEpsilon);
        assertEquals(expected_angle_rads, pose3.getRotation().getRadians(), kTestEpsilon);

        pose1 = new RigidTransform2d(new Translation2d(3, 4), Rotation2d.fromDegrees(90));
        pose2 = new RigidTransform2d(new Translation2d(13, -6), Rotation2d.fromDegrees(0.0));
        pose3 = pose1.interpolate(pose2, .75);
        expected_angle_rads = Math.PI / 8;
        assertEquals(3.0 + 10.0 * Math.cos(expected_angle_rads), pose3.getTranslation().x(), kTestEpsilon);
        assertEquals(-6.0 + 10.0 * Math.sin(expected_angle_rads), pose3.getTranslation().y(), kTestEpsilon);
        assertEquals(expected_angle_rads, pose3.getRotation().getRadians(), kTestEpsilon);
    }

    @Test
    public void testTwist() {
        // Exponentiation (integrate twist to obtain a RigidTransform2d)
        Twist2d twist = new Twist2d(1.0, 0.0, 0.0);
        RigidTransform2d pose = RigidTransform2d.exp(twist);
        assertEquals(1.0, pose.getTranslation().x(), kTestEpsilon);
        assertEquals(0.0, pose.getTranslation().y(), kTestEpsilon);
        assertEquals(0.0, pose.getRotation().getDegrees(), kTestEpsilon);

        // Scaled.
        twist = new Twist2d(1.0, 0.0, 0.0);
        pose = RigidTransform2d.exp(twist.scaled(2.5));
        assertEquals(2.5, pose.getTranslation().x(), kTestEpsilon);
        assertEquals(0.0, pose.getTranslation().y(), kTestEpsilon);
        assertEquals(0.0, pose.getRotation().getDegrees(), kTestEpsilon);

        // Logarithm (find the twist to apply to obtain a given RigidTransform2d)
        pose = new RigidTransform2d(new Translation2d(2.0, 2.0), Rotation2d.fromRadians(Math.PI / 2));
        twist = RigidTransform2d.log(pose);
        assertEquals(Math.PI, twist.dx, kTestEpsilon);
        assertEquals(0.0, twist.dy, kTestEpsilon);
        assertEquals(Math.PI / 2, twist.dtheta, kTestEpsilon);

        // Logarithm is the inverse of exponentiation.
        RigidTransform2d new_pose = RigidTransform2d.exp(twist);
        assertEquals(new_pose.getTranslation().x(), pose.getTranslation().x(), kTestEpsilon);
        assertEquals(new_pose.getTranslation().y(), pose.getTranslation().y(), kTestEpsilon);
        assertEquals(new_pose.getRotation().getDegrees(), pose.getRotation().getDegrees(), kTestEpsilon);
    }

    @Test
    public void testPolynomialRegression() {
        double[] x = { 0, 1, 2, 3, 4, 5 };
        double[] y = { 0, 2, 4, 6, 8, 10 };
        PolynomialRegression regression = new PolynomialRegression(x, y, 1);

        assertEquals(regression.degree(), 1);
        assertEquals(regression.beta(1), 2.0, kTestEpsilon);
        assertEquals(regression.beta(0), 0.0, kTestEpsilon);
        assertEquals(regression.predict(2.5), 5.0, kTestEpsilon);

        regression = Constants.kFlywheelAutoAimPolynomial;
        System.out.println(regression);
    }
}
