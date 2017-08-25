package com.team254.frc2017.paths;

import com.team254.frc2017.auto.modes.BoilerGearThenShootModeRed;
import com.team254.frc2017.paths.PathBuilder.Waypoint;
import com.team254.lib.util.control.Path;
import com.team254.lib.util.math.RigidTransform2d;
import com.team254.lib.util.math.Rotation2d;
import com.team254.lib.util.math.Translation2d;

import java.util.ArrayList;

/**
 * Path from the red boiler side peg to the red boiler.
 * 
 * Used in BoilerGearThenShootModeRed
 * 
 * @see BoilerGearThenShootModeRed
 * @see PathContainer
 */
public class BoilerGearToShootRed implements PathContainer {

    @Override
    public Path buildPath() {
        ArrayList<Waypoint> sWaypoints = new ArrayList<Waypoint>();
        sWaypoints.add(new Waypoint(112, 115, 0, 0));
        sWaypoints.add(new Waypoint(97, 89, 0, 60));

        // sWaypoints.add(new Waypoint(89,160,0,0));
        // sWaypoints.add(new Waypoint(40,160,36,80));
        // sWaypoints.add(new Waypoint(40,100,0,80));

        return PathBuilder.buildPathFromWaypoints(sWaypoints);
    }

    @Override
    public RigidTransform2d getStartPose() {
        return new RigidTransform2d(new Translation2d(120, 110), Rotation2d.fromDegrees(0.0));
    }

    @Override
    public boolean isReversed() {
        return false;
    }
    // WAYPOINT_DATA:
    // [{"position":{"x":120,"y":215},"speed":0,"radius":0,"comment":""},{"position":{"x":90,"y":245},"speed":120,"radius":0,"comment":""}]
    // IS_REVERSED: true
    // FILE_NAME: GearToShootBlue
}