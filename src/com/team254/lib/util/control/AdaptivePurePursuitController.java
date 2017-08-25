package com.team254.lib.util.control;

import com.team254.lib.util.math.RigidTransform2d;
import com.team254.lib.util.math.Rotation2d;
import com.team254.lib.util.math.Translation2d;
import com.team254.lib.util.math.Twist2d;

/**
 * Implements an adaptive pure pursuit controller. See:
 * https://www.ri.cmu.edu/pub_files/pub1/kelly_alonzo_1994_4/kelly_alonzo_1994_4 .pdf
 * 
 * Basically, we find a spot on the path we'd like to follow and calculate the arc necessary to make us land on that
 * spot. The target spot is a specified distance ahead of us, and we look further ahead the greater our tracking error.
 * We also return the maximum speed we'd like to be going when we reach the target spot.
 */

public class AdaptivePurePursuitController {
    private static final double kReallyBigNumber = 1E6;

    public static class Command {
        public Twist2d delta = Twist2d.identity();
        public double cross_track_error;
        public double max_velocity;
        public double end_velocity;
        public Translation2d lookahead_point;
        public double remaining_path_length;

        public Command() {
        }

        public Command(Twist2d delta, double cross_track_error, double max_velocity, double end_velocity,
                Translation2d lookahead_point, double remaining_path_length) {
            this.delta = delta;
            this.cross_track_error = cross_track_error;
            this.max_velocity = max_velocity;
            this.end_velocity = end_velocity;
            this.lookahead_point = lookahead_point;
            this.remaining_path_length = remaining_path_length;
        }
    }

    Path mPath;
    boolean mAtEndOfPath = false;
    final boolean mReversed;
    final Lookahead mLookahead;

    public AdaptivePurePursuitController(Path path, boolean reversed, Lookahead lookahead) {
        mPath = path;
        mReversed = reversed;
        mLookahead = lookahead;
    }

    /**
     * Gives the RigidTransform2d.Delta that the robot should take to follow the path
     * 
     * @param pose
     *            robot pose
     * @return movement command for the robot to follow
     */
    public Command update(RigidTransform2d pose) {
        if (mReversed) {
            pose = new RigidTransform2d(pose.getTranslation(),
                    pose.getRotation().rotateBy(Rotation2d.fromRadians(Math.PI)));
        }

        final Path.TargetPointReport report = mPath.getTargetPoint(pose.getTranslation(), mLookahead);
        if (isFinished()) {
            // Stop.
            return new Command(Twist2d.identity(), report.closest_point_distance, report.max_speed, 0.0,
                    report.lookahead_point, report.remaining_path_distance);
        }

        final Arc arc = new Arc(pose, report.lookahead_point);
        double scale_factor = 1.0;
        // Ensure we don't overshoot the end of the path (once the lookahead speed drops to zero).
        if (report.lookahead_point_speed < 1E-6 && report.remaining_path_distance < arc.length) {
            scale_factor = Math.max(0.0, report.remaining_path_distance / arc.length);
            mAtEndOfPath = true;
        } else {
            mAtEndOfPath = false;
        }
        if (mReversed) {
            scale_factor *= -1;
        }

        return new Command(
                new Twist2d(scale_factor * arc.length, 0.0,
                        arc.length * getDirection(pose, report.lookahead_point) * Math.abs(scale_factor) / arc.radius),
                report.closest_point_distance, report.max_speed,
                report.lookahead_point_speed * Math.signum(scale_factor), report.lookahead_point,
                report.remaining_path_distance);
    }

    public boolean hasPassedMarker(String marker) {
        return mPath.hasPassedMarker(marker);
    }

    public static class Arc {
        public Translation2d center;
        public double radius;
        public double length;

        public Arc(RigidTransform2d pose, Translation2d point) {
            center = getCenter(pose, point);
            radius = new Translation2d(center, point).norm();
            length = getLength(pose, point, center, radius);
        }
    }

    /**
     * Gives the center of the circle joining the lookahead point and robot pose
     * 
     * @param pose
     *            robot pose
     * @param point
     *            lookahead point
     * @return center of the circle joining the lookahead point and robot pose
     */
    public static Translation2d getCenter(RigidTransform2d pose, Translation2d point) {
        final Translation2d poseToPointHalfway = pose.getTranslation().interpolate(point, 0.5);
        final Rotation2d normal = pose.getTranslation().inverse().translateBy(poseToPointHalfway).direction().normal();
        final RigidTransform2d perpendicularBisector = new RigidTransform2d(poseToPointHalfway, normal);
        final RigidTransform2d normalFromPose = new RigidTransform2d(pose.getTranslation(),
                pose.getRotation().normal());
        if (normalFromPose.isColinear(perpendicularBisector.normal())) {
            // Special case: center is poseToPointHalfway.
            return poseToPointHalfway;
        }
        return normalFromPose.intersection(perpendicularBisector);
    }

    /**
     * Gives the radius of the circle joining the lookahead point and robot pose
     * 
     * @param pose
     *            robot pose
     * @param point
     *            lookahead point
     * @return radius of the circle joining the lookahead point and robot pose
     */
    public static double getRadius(RigidTransform2d pose, Translation2d point) {
        Translation2d center = getCenter(pose, point);
        return new Translation2d(center, point).norm();
    }

    /**
     * Gives the length of the arc joining the lookahead point and robot pose (assuming forward motion).
     * 
     * @param pose
     *            robot pose
     * @param point
     *            lookahead point
     * @return the length of the arc joining the lookahead point and robot pose
     */
    public static double getLength(RigidTransform2d pose, Translation2d point) {
        final double radius = getRadius(pose, point);
        final Translation2d center = getCenter(pose, point);
        return getLength(pose, point, center, radius);
    }

    public static double getLength(RigidTransform2d pose, Translation2d point, Translation2d center, double radius) {
        if (radius < kReallyBigNumber) {
            final Translation2d centerToPoint = new Translation2d(center, point);
            final Translation2d centerToPose = new Translation2d(center, pose.getTranslation());
            // If the point is behind pose, we want the opposite of this angle. To determine if the point is behind,
            // check the sign of the cross-product between the normal vector and the vector from pose to point.
            final boolean behind = Math.signum(
                    Translation2d.cross(pose.getRotation().normal().toTranslation(),
                            new Translation2d(pose.getTranslation(), point))) > 0.0;
            final Rotation2d angle = Translation2d.getAngle(centerToPose, centerToPoint);
            return radius * (behind ? 2.0 * Math.PI - Math.abs(angle.getRadians()) : Math.abs(angle.getRadians()));
        } else {
            return new Translation2d(pose.getTranslation(), point).norm();
        }
    }

    /**
     * Gives the direction the robot should turn to stay on the path
     * 
     * @param pose
     *            robot pose
     * @param point
     *            lookahead point
     * @return the direction the robot should turn: -1 is left, +1 is right
     */
    public static int getDirection(RigidTransform2d pose, Translation2d point) {
        Translation2d poseToPoint = new Translation2d(pose.getTranslation(), point);
        Translation2d robot = pose.getRotation().toTranslation();
        double cross = robot.x() * poseToPoint.y() - robot.y() * poseToPoint.x();
        return (cross < 0) ? -1 : 1; // if robot < pose turn left
    }

    /**
     * @return has the robot reached the end of the path
     */
    public boolean isFinished() {
        return mAtEndOfPath;
    }
}