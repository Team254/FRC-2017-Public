package com.team254.frc2017.paths;

import com.team254.frc2017.auto.actions.WaitForPathMarkerAction;
import com.team254.lib.util.control.Path;
import com.team254.lib.util.control.PathSegment;
import com.team254.lib.util.math.RigidTransform2d;
import com.team254.lib.util.math.Rotation2d;
import com.team254.lib.util.math.Translation2d;

import java.util.List;

/**
 * Class used to convert a list of Waypoints into a Path object consisting of arc and line PathSegments
 * 
 * @see Waypoint
 * @see Path
 * @see PathSegment
 */
public class PathBuilder {
    private static final double kEpsilon = 1E-9;
    private static final double kReallyBigNumber = 1E9;

    public static Path buildPathFromWaypoints(List<Waypoint> w) {
        Path p = new Path();
        if (w.size() < 2)
            throw new Error("Path must contain at least 2 waypoints");
        int i = 0;
        if (w.size() > 2) {
            do {
                new Arc(getPoint(w, i), getPoint(w, i + 1), getPoint(w, i + 2)).addToPath(p);
                i++;
            } while (i < w.size() - 2);
        }
        new Line(w.get(w.size() - 2), w.get(w.size() - 1)).addToPath(p, 0);
        p.extrapolateLast();
        p.verifySpeeds();
        // System.out.println(p);
        return p;
    }

    private static Waypoint getPoint(List<Waypoint> w, int i) {
        if (i > w.size())
            return w.get(w.size() - 1);
        return w.get(i);
    }

    /**
     * A waypoint along a path. Contains a position, radius (for creating curved paths), and speed. The information from
     * these waypoints is used by the PathBuilder class to generate Paths. Waypoints also contain an optional marker
     * that is used by the WaitForPathMarkerAction.
     *
     * @see PathBuilder
     * @see WaitForPathMarkerAction
     */
    public static class Waypoint {
        Translation2d position;
        double radius;
        double speed;
        String marker;

        public Waypoint(Waypoint other) {
            this(other.position.x(), other.position.y(), other.radius, other.speed, other.marker);
        }

        public Waypoint(double x, double y, double r, double s) {
            position = new Translation2d(x, y);
            radius = r;
            speed = s;
        }

        public Waypoint(Translation2d pos, double r, double s) {
            position = pos;
            radius = r;
            speed = s;
        }

        public Waypoint(double x, double y, double r, double s, String m) {
            position = new Translation2d(x, y);
            radius = r;
            speed = s;
            marker = m;
        }
    }

    /**
     * A Line object is formed by two Waypoints. Contains a start and end position, slope, and speed.
     */
    static class Line {
        Waypoint a;
        Waypoint b;
        Translation2d start;
        Translation2d end;
        Translation2d slope;
        double speed;

        public Line(Waypoint a, Waypoint b) {
            this.a = a;
            this.b = b;
            slope = new Translation2d(a.position, b.position);
            speed = b.speed;
            start = a.position.translateBy(slope.scale(a.radius / slope.norm()));
            end = b.position.translateBy(slope.scale(-b.radius / slope.norm()));
        }

        private void addToPath(Path p, double endSpeed) {
            double pathLength = new Translation2d(end, start).norm();
            if (pathLength > kEpsilon) {
                if (b.marker != null) {
                    p.addSegment(new PathSegment(start.x(), start.y(), end.x(), end.y(), b.speed,
                            p.getLastMotionState(), endSpeed, b.marker));
                } else {
                    p.addSegment(new PathSegment(start.x(), start.y(), end.x(), end.y(), b.speed,
                            p.getLastMotionState(), endSpeed));
                }
            }

        }
    }

    /**
     * An Arc object is formed by two Lines that share a common Waypoint. Contains a center position, radius, and speed.
     */
    static class Arc {
        Line a;
        Line b;
        Translation2d center;
        double radius;
        double speed;

        public Arc(Waypoint a, Waypoint b, Waypoint c) {
            this(new Line(a, b), new Line(b, c));
        }

        public Arc(Line a, Line b) {
            this.a = a;
            this.b = b;
            this.speed = (a.speed + b.speed) / 2;
            this.center = intersect(a, b);
            this.radius = new Translation2d(center, a.end).norm();
        }

        private void addToPath(Path p) {
            a.addToPath(p, speed);
            if (radius > kEpsilon && radius < kReallyBigNumber) {
                p.addSegment(new PathSegment(a.end.x(), a.end.y(), b.start.x(), b.start.y(), center.x(), center.y(),
                        speed, p.getLastMotionState(), b.speed));
            }
        }

        private static Translation2d intersect(Line l1, Line l2) {
            final RigidTransform2d lineA = new RigidTransform2d(l1.end, new Rotation2d(l1.slope, true).normal());
            final RigidTransform2d lineB = new RigidTransform2d(l2.start, new Rotation2d(l2.slope, true).normal());
            return lineA.intersection(lineB);
        }
    }
}
