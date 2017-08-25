package com.team254.frc2017.paths;

import com.team254.frc2017.auto.modes.RamHopperShootModeRed;
import com.team254.frc2017.paths.PathBuilder.Waypoint;
import com.team254.lib.util.control.Path;
import com.team254.lib.util.math.RigidTransform2d;
import com.team254.lib.util.math.Rotation2d;
import com.team254.lib.util.math.Translation2d;

import java.util.ArrayList;

/**
 * Path from the red alliance wall to the red hopper.
 * 
 * Used in RamHopperShootModeRed
 * 
 * @see RamHopperShootModeRed
 * @see PathContainer
 */
public class StartToHopperRed implements PathContainer {
    @Override
    public Path buildPath() {
        ArrayList<Waypoint> sWaypoints = new ArrayList<Waypoint>();
        sWaypoints.add(new Waypoint(16, 90, 0, 0));
        sWaypoints.add(new Waypoint(97, 90, 54, 90));
        sWaypoints.add(new Waypoint(97, 29, 0, 90, "RamWall"));
        sWaypoints.add(new Waypoint(97, 24, 0, 40));
        sWaypoints.add(new Waypoint(97, 4, 0, 40));

        return PathBuilder.buildPathFromWaypoints(sWaypoints);
    }

    @Override
    public RigidTransform2d getStartPose() {
        return new RigidTransform2d(new Translation2d(16, 90), Rotation2d.fromDegrees(0.0));
    }

    @Override
    public boolean isReversed() {
        return false;
    }
    // WAYPOINT_DATA:
    // [{"position":{"x":16,"y":90},"speed":0,"radius":0,"comment":""},{"position":{"x":92,"y":90},"speed":90,"radius":54,"comment":""},{"position":{"x":92,"y":29},"speed":90,"radius":0,"comment":""},{"position":{"x":92,"y":24},"speed":40,"radius":0,"comment":""},{"position":{"x":92,"y":4},"speed":40,"radius":0,"comment":""}]
    // IS_REVERSED: false
    // FILE_NAME: StartToHopperRed
}