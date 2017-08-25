package com.team254.frc2017.paths;

import com.team254.frc2017.auto.modes.CenterGearThenShootModeBlue;
import com.team254.frc2017.paths.PathBuilder.Waypoint;
import com.team254.lib.util.control.Path;
import com.team254.lib.util.math.RigidTransform2d;
import com.team254.lib.util.math.Rotation2d;
import com.team254.lib.util.math.Translation2d;

import java.util.ArrayList;

/**
 * Path from the blue center peg to the blue boiler.
 * 
 * Used in CenterGearThenShootModeBlue
 * 
 * @see CenterGearThenShootModeBlue
 * @see PathContainer
 */
public class CenterGearToShootBlue implements PathContainer {

    @Override
    public Path buildPath() {
        ArrayList<Waypoint> sWaypoints = new ArrayList<Waypoint>();
        sWaypoints.add(new Waypoint(86, 160, 0, 0));
        sWaypoints.add(new Waypoint(36, 160, 36, 80));
        sWaypoints.add(new Waypoint(36, 230, 0, 80));

        return PathBuilder.buildPathFromWaypoints(sWaypoints);
    }

    @Override
    public RigidTransform2d getStartPose() {
        return new RigidTransform2d(new Translation2d(90, 160), Rotation2d.fromDegrees(180.0));
    }

    @Override
    public boolean isReversed() {
        return false;
    }
    // WAYPOINT_DATA:
    // [{"position":{"x":90,"y":160},"speed":0,"radius":0,"comment":""},{"position":{"x":40,"y":160},"speed":60,"radius":40,"comment":""},{"position":{"x":40,"y":220},"speed":60,"radius":0,"comment":""}]
    // IS_REVERSED: false
    // FILE_NAME: CenterGearToShootBlue
}