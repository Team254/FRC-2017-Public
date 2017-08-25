package com.team254.frc2017;

import com.team254.lib.util.math.Rotation2d;

/**
 * A container class to specify the shooter angle. It contains the desired range, the field_to_goal_angle
 */
public class ShooterAimingParameters {
    double range;
    double last_seen_timestamp;
    double stability;
    Rotation2d robot_to_goal;

    public ShooterAimingParameters(double range, Rotation2d robot_to_goal, double last_seen_timestamp,
            double stability) {
        this.range = range;
        this.robot_to_goal = robot_to_goal;
        this.last_seen_timestamp = last_seen_timestamp;
        this.stability = stability;
    }

    public double getRange() {
        return range;
    }

    public Rotation2d getRobotToGoal() {
        return robot_to_goal;
    }

    public double getLastSeenTimestamp() {
        return last_seen_timestamp;
    }

    public double getStability() {
        return stability;
    }

}
