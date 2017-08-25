package com.team254.frc2017.paths;

import com.team254.frc2017.paths.PathBuilder.Waypoint;
import com.team254.lib.util.control.Path;
import com.team254.lib.util.math.RigidTransform2d;
import com.team254.lib.util.math.Rotation2d;
import com.team254.lib.util.math.Translation2d;

import java.util.ArrayList;

/**
 * Path from the red alliance wall to the red center peg.
 * 
 * Used in CenterGearToShootRed
 * 
 * @see CenterGearToShootRed
 * @see PathContainer
 */
public class StartToCenterGearRed implements PathContainer {

    @Override
    public Path buildPath() {
        ArrayList<Waypoint> sWaypoints = new ArrayList<Waypoint>();
        ;
        sWaypoints.add(new Waypoint(16, 160, 0, 0));
        sWaypoints.add(new Waypoint(89, 160, 0, 40));

        return PathBuilder.buildPathFromWaypoints(sWaypoints);
    }

    @Override
    public RigidTransform2d getStartPose() {
        return new RigidTransform2d(new Translation2d(16, 160), Rotation2d.fromDegrees(180.0));
    }

    @Override
    public boolean isReversed() {
        return true;
    }
    // WAYPOINT_DATA:
    // [{"position":{"x":16,"y":89},"speed":0,"radius":0,"comment":""},{"position":{"x":80,"y":89},"speed":30,"radius":0,"comment":""},{"position":{"x":109,"y":121},"speed":30,"radius":0,"comment":""}]
    // IS_REVERSED: true
    // FILE_NAME: StartToGearRed
}