package com.team254.frc2017.vision;

/**
 * A container class for Targets detected by the vision system, containing the location in three-dimensional space.
 */
public class TargetInfo {
    protected double x = 1.0;
    protected double y;
    protected double z;

    public TargetInfo(double y, double z) {
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}