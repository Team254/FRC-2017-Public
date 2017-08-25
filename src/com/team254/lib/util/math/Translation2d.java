package com.team254.lib.util.math;

import com.team254.lib.util.Interpolable;

import java.text.DecimalFormat;

/**
 * A translation in a 2d coordinate frame. Translations are simply shifts in an (x, y) plane.
 */
public class Translation2d implements Interpolable<Translation2d> {
    protected static final Translation2d kIdentity = new Translation2d();

    public static final Translation2d identity() {
        return kIdentity;
    }

    protected double x_;
    protected double y_;

    public Translation2d() {
        x_ = 0;
        y_ = 0;
    }

    public Translation2d(double x, double y) {
        x_ = x;
        y_ = y;
    }

    public Translation2d(Translation2d other) {
        x_ = other.x_;
        y_ = other.y_;
    }

    public Translation2d(Translation2d start, Translation2d end) {
        x_ = end.x_ - start.x_;
        y_ = end.y_ - start.y_;
    }

    /**
     * The "norm" of a transform is the Euclidean distance in x and y.
     * 
     * @return sqrt(x^2 + y^2)
     */
    public double norm() {
        return Math.hypot(x_, y_);
    }

    public double norm2() {
        return x_ * x_ + y_ * y_;
    }

    public double x() {
        return x_;
    }

    public double y() {
        return y_;
    }

    public void setX(double x) {
        x_ = x;
    }

    public void setY(double y) {
        y_ = y;
    }

    /**
     * We can compose Translation2d's by adding together the x and y shifts.
     * 
     * @param other
     *            The other translation to add.
     * @return The combined effect of translating by this object and the other.
     */
    public Translation2d translateBy(Translation2d other) {
        return new Translation2d(x_ + other.x_, y_ + other.y_);
    }

    /**
     * We can also rotate Translation2d's. See: https://en.wikipedia.org/wiki/Rotation_matrix
     * 
     * @param rotation
     *            The rotation to apply.
     * @return This translation rotated by rotation.
     */
    public Translation2d rotateBy(Rotation2d rotation) {
        return new Translation2d(x_ * rotation.cos() - y_ * rotation.sin(), x_ * rotation.sin() + y_ * rotation.cos());
    }

    public Rotation2d direction() {
        return new Rotation2d(x_, y_, true);
    }

    /**
     * The inverse simply means a Translation2d that "undoes" this object.
     * 
     * @return Translation by -x and -y.
     */
    public Translation2d inverse() {
        return new Translation2d(-x_, -y_);
    }

    @Override
    public Translation2d interpolate(Translation2d other, double x) {
        if (x <= 0) {
            return new Translation2d(this);
        } else if (x >= 1) {
            return new Translation2d(other);
        }
        return extrapolate(other, x);
    }

    public Translation2d extrapolate(Translation2d other, double x) {
        return new Translation2d(x * (other.x_ - x_) + x_, x * (other.y_ - y_) + y_);
    }

    public Translation2d scale(double s) {
        return new Translation2d(x_ * s, y_ * s);
    }

    @Override
    public String toString() {
        final DecimalFormat fmt = new DecimalFormat("#0.000");
        return "(" + fmt.format(x_) + "," + fmt.format(y_) + ")";
    }

    public static double dot(Translation2d a, Translation2d b) {
        return a.x_ * b.x_ + a.y_ * b.y_;
    }

    public static Rotation2d getAngle(Translation2d a, Translation2d b) {
        double cos_angle = dot(a, b) / (a.norm() * b.norm());
        if (Double.isNaN(cos_angle)) {
            return new Rotation2d();
        }
        return Rotation2d.fromRadians(Math.acos(Math.min(1.0, Math.max(cos_angle, -1.0))));
    }

    public static double cross(Translation2d a, Translation2d b) {
        return a.x_ * b.y_ - a.y_ * b.x_;
    }
}
