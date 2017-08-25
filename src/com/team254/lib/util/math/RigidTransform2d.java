package com.team254.lib.util.math;

import static com.team254.lib.util.Util.epsilonEquals;

import com.team254.lib.util.Interpolable;

/**
 * Represents a 2d pose (rigid transform) containing translational and rotational elements.
 * 
 * Inspired by Sophus (https://github.com/strasdat/Sophus/tree/master/sophus)
 */
public class RigidTransform2d implements Interpolable<RigidTransform2d> {
    protected static final double kEpsilon = 1E-9;

    protected static final RigidTransform2d kIdentity = new RigidTransform2d();

    public static final RigidTransform2d identity() {
        return kIdentity;
    }

    private final static double kEps = 1E-9;

    protected Translation2d translation_;
    protected Rotation2d rotation_;

    public RigidTransform2d() {
        translation_ = new Translation2d();
        rotation_ = new Rotation2d();
    }

    public RigidTransform2d(Translation2d translation, Rotation2d rotation) {
        translation_ = translation;
        rotation_ = rotation;
    }

    public RigidTransform2d(RigidTransform2d other) {
        translation_ = new Translation2d(other.translation_);
        rotation_ = new Rotation2d(other.rotation_);
    }

    public static RigidTransform2d fromTranslation(Translation2d translation) {
        return new RigidTransform2d(translation, new Rotation2d());
    }

    public static RigidTransform2d fromRotation(Rotation2d rotation) {
        return new RigidTransform2d(new Translation2d(), rotation);
    }

    /**
     * Obtain a new RigidTransform2d from a (constant curvature) velocity. See:
     * https://github.com/strasdat/Sophus/blob/master/sophus/se2.hpp
     */
    public static RigidTransform2d exp(Twist2d delta) {
        double sin_theta = Math.sin(delta.dtheta);
        double cos_theta = Math.cos(delta.dtheta);
        double s, c;
        if (Math.abs(delta.dtheta) < kEps) {
            s = 1.0 - 1.0 / 6.0 * delta.dtheta * delta.dtheta;
            c = .5 * delta.dtheta;
        } else {
            s = sin_theta / delta.dtheta;
            c = (1.0 - cos_theta) / delta.dtheta;
        }
        return new RigidTransform2d(new Translation2d(delta.dx * s - delta.dy * c, delta.dx * c + delta.dy * s),
                new Rotation2d(cos_theta, sin_theta, false));
    }

    /**
     * Logical inverse of the above.
     */
    public static Twist2d log(RigidTransform2d transform) {
        final double dtheta = transform.getRotation().getRadians();
        final double half_dtheta = 0.5 * dtheta;
        final double cos_minus_one = transform.getRotation().cos() - 1.0;
        double halftheta_by_tan_of_halfdtheta;
        if (Math.abs(cos_minus_one) < kEps) {
            halftheta_by_tan_of_halfdtheta = 1.0 - 1.0 / 12.0 * dtheta * dtheta;
        } else {
            halftheta_by_tan_of_halfdtheta = -(half_dtheta * transform.getRotation().sin()) / cos_minus_one;
        }
        final Translation2d translation_part = transform.getTranslation()
                .rotateBy(new Rotation2d(halftheta_by_tan_of_halfdtheta, -half_dtheta, false));
        return new Twist2d(translation_part.x(), translation_part.y(), dtheta);
    }

    public Translation2d getTranslation() {
        return translation_;
    }

    public void setTranslation(Translation2d translation) {
        translation_ = translation;
    }

    public Rotation2d getRotation() {
        return rotation_;
    }

    public void setRotation(Rotation2d rotation) {
        rotation_ = rotation;
    }

    /**
     * Transforming this RigidTransform2d means first translating by other.translation and then rotating by
     * other.rotation
     * 
     * @param other
     *            The other transform.
     * @return This transform * other
     */
    public RigidTransform2d transformBy(RigidTransform2d other) {
        return new RigidTransform2d(translation_.translateBy(other.translation_.rotateBy(rotation_)),
                rotation_.rotateBy(other.rotation_));
    }

    /**
     * The inverse of this transform "undoes" the effect of translating by this transform.
     * 
     * @return The opposite of this transform.
     */
    public RigidTransform2d inverse() {
        Rotation2d rotation_inverted = rotation_.inverse();
        return new RigidTransform2d(translation_.inverse().rotateBy(rotation_inverted), rotation_inverted);
    }

    public RigidTransform2d normal() {
        return new RigidTransform2d(translation_, rotation_.normal());
    }

    /**
     * Finds the point where the heading of this transform intersects the heading of another. Returns (+INF, +INF) if
     * parallel.
     */
    public Translation2d intersection(RigidTransform2d other) {
        final Rotation2d other_rotation = other.getRotation();
        if (rotation_.isParallel(other_rotation)) {
            // Lines are parallel.
            return new Translation2d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
        if (Math.abs(rotation_.cos()) < Math.abs(other_rotation.cos())) {
            return intersectionInternal(this, other);
        } else {
            return intersectionInternal(other, this);
        }
    }

    /**
     * Return true if the heading of this transform is colinear with the heading of another.
     */
    public boolean isColinear(RigidTransform2d other) {
        final Twist2d twist = log(inverse().transformBy(other));
        return (epsilonEquals(twist.dy, 0.0, kEpsilon) && epsilonEquals(twist.dtheta, 0.0, kEpsilon));
    }

    private static Translation2d intersectionInternal(RigidTransform2d a, RigidTransform2d b) {
        final Rotation2d a_r = a.getRotation();
        final Rotation2d b_r = b.getRotation();
        final Translation2d a_t = a.getTranslation();
        final Translation2d b_t = b.getTranslation();

        final double tan_b = b_r.tan();
        final double t = ((a_t.x() - b_t.x()) * tan_b + b_t.y() - a_t.y())
                / (a_r.sin() - a_r.cos() * tan_b);
        return a_t.translateBy(a_r.toTranslation().scale(t));
    }

    /**
     * Do twist interpolation of this transform assuming constant curvature.
     */
    @Override
    public RigidTransform2d interpolate(RigidTransform2d other, double x) {
        if (x <= 0) {
            return new RigidTransform2d(this);
        } else if (x >= 1) {
            return new RigidTransform2d(other);
        }
        final Twist2d twist = RigidTransform2d.log(inverse().transformBy(other));
        return transformBy(RigidTransform2d.exp(twist.scaled(x)));
    }

    @Override
    public String toString() {
        return "T:" + translation_.toString() + ", R:" + rotation_.toString();
    }
}
