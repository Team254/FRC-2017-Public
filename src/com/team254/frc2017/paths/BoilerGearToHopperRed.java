package com.team254.frc2017.paths;

import com.team254.frc2017.auto.modes.GearThenHopperShootModeRed;
import com.team254.frc2017.paths.profiles.PathAdapter;
import com.team254.lib.util.control.Path;
import com.team254.lib.util.math.RigidTransform2d;
import com.team254.lib.util.math.Rotation2d;
import com.team254.lib.util.math.Translation2d;

/**
 * Path from the red boiler side peg to the red hopper.
 * 
 * Used in GearThenHopperShootModeRed
 * 
 * @see GearThenHopperShootModeRed
 * @see PathContainer
 */
public class BoilerGearToHopperRed implements PathContainer {

    @Override
    public Path buildPath() {
        return PathAdapter.getRedHopperPath();
    }

    @Override
    public RigidTransform2d getStartPose() {
        return new RigidTransform2d(new Translation2d(112, 115), Rotation2d.fromDegrees(0.0));
    }

    @Override
    public boolean isReversed() {
        return false;
    }

}
