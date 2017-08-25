package com.team254.frc2017.paths;

import com.team254.frc2017.auto.modes.GearThenHopperShootModeBlue;
import com.team254.frc2017.paths.profiles.PathAdapter;
import com.team254.lib.util.control.Path;
import com.team254.lib.util.math.RigidTransform2d;
import com.team254.lib.util.math.Rotation2d;
import com.team254.lib.util.math.Translation2d;

/**
 * Path from the blue boiler side peg to the blue hopper.
 * 
 * Used in GearThenHopperShootModeBlue
 * 
 * @see GearThenHopperShootModeBlue
 * @see PathContainer
 */
public class BoilerGearToHopperBlue implements PathContainer {

    @Override
    public Path buildPath() {
        return PathAdapter.getBlueHopperPath();
    }

    @Override
    public RigidTransform2d getStartPose() {
        return new RigidTransform2d(new Translation2d(116, 209), Rotation2d.fromDegrees(0.0));
    }

    @Override
    public boolean isReversed() {
        return false;
    }

}
