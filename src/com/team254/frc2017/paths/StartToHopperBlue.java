package com.team254.frc2017.paths;

import com.team254.frc2017.auto.modes.RamHopperShootModeBlue;
import com.team254.frc2017.paths.PathBuilder.Waypoint;
import com.team254.lib.util.control.Path;
import com.team254.lib.util.math.RigidTransform2d;
import com.team254.lib.util.math.Rotation2d;
import com.team254.lib.util.math.Translation2d;

import java.util.ArrayList;

/**
 * Path from the blue alliance wall to the blue hopper.
 * 
 * Used in RamHopperShootModeBlue
 * 
 * @see RamHopperShootModeBlue
 * @see PathContainer
 */
public class StartToHopperBlue implements PathContainer {

    @Override
    public Path buildPath() {
        ArrayList<Waypoint> sWaypoints = new ArrayList<Waypoint>();
        sWaypoints.add(new Waypoint(16, 234, 0, 0));
        sWaypoints.add(new Waypoint(97, 234, 54, 90));
        sWaypoints.add(new Waypoint(97, 295, 0, 90, "RamWall"));
        sWaypoints.add(new Waypoint(97, 300, 0, 40));
        sWaypoints.add(new Waypoint(97, 340, 0, 40));

        return PathBuilder.buildPathFromWaypoints(sWaypoints);
    }

    @Override
    public RigidTransform2d getStartPose() {
        return new RigidTransform2d(new Translation2d(16, 234), Rotation2d.fromDegrees(0.0));
    }

    @Override
    public boolean isReversed() {
        return false;
    }
    // WAYPOINT_DATA:
    // [{"position":{"x":16,"y":234},"speed":0,"radius":0,"comment":""},{"position":{"x":95,"y":234},"speed":60,"radius":48,"comment":""},{"position":{"x":95,"y":300},"speed":60,"radius":0,"comment":""}]
    // IS_REVERSED: false
    // FILE_NAME: StartToHopperBlue
}