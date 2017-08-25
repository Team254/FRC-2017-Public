package com.team254.lib.util.control;

import com.team254.frc2017.Constants;
import com.team254.lib.util.math.Rotation2d;
import com.team254.lib.util.math.Translation2d;
import com.team254.lib.util.motion.MotionProfile;
import com.team254.lib.util.motion.MotionProfileConstraints;
import com.team254.lib.util.motion.MotionProfileGenerator;
import com.team254.lib.util.motion.MotionProfileGoal;
import com.team254.lib.util.motion.MotionState;

import java.util.Optional;

/**
 * Class representing a segment of the robot's autonomous path.
 */

public class PathSegment {
    private Translation2d start;
    private Translation2d end;
    private Translation2d center;
    private Translation2d deltaStart;
    private Translation2d deltaEnd;
    private double maxSpeed;
    private boolean isLine;
    private MotionProfile speedController;
    private boolean extrapolateLookahead;
    private String marker;

    /**
     * Constructor for a linear segment
     * 
     * @param x1
     *            start x
     * @param y1
     *            start y
     * @param x2
     *            end x
     * @param y2
     *            end y
     * @param maxSpeed
     *            maximum speed allowed on the segment
     */
    public PathSegment(double x1, double y1, double x2, double y2, double maxSpeed, MotionState startState,
            double endSpeed) {
        this.start = new Translation2d(x1, y1);
        this.end = new Translation2d(x2, y2);

        this.deltaStart = new Translation2d(start, end);

        this.maxSpeed = maxSpeed;
        extrapolateLookahead = false;
        isLine = true;
        createMotionProfiler(startState, endSpeed);
    }

    public PathSegment(double x1, double y1, double x2, double y2, double maxSpeed, MotionState startState,
            double endSpeed, String marker) {
        this.start = new Translation2d(x1, y1);
        this.end = new Translation2d(x2, y2);

        this.deltaStart = new Translation2d(start, end);

        this.maxSpeed = maxSpeed;
        extrapolateLookahead = false;
        isLine = true;
        this.marker = marker;
        createMotionProfiler(startState, endSpeed);
    }

    /**
     * Constructor for an arc segment
     * 
     * @param x1
     *            start x
     * @param y1
     *            start y
     * @param x2
     *            end x
     * @param y2
     *            end y
     * @param cx
     *            center x
     * @param cy
     *            center y
     * @param maxSpeed
     *            maximum speed allowed on the segment
     */
    public PathSegment(double x1, double y1, double x2, double y2, double cx, double cy, double maxSpeed,
            MotionState startState, double endSpeed) {
        this.start = new Translation2d(x1, y1);
        this.end = new Translation2d(x2, y2);
        this.center = new Translation2d(cx, cy);

        this.deltaStart = new Translation2d(center, start);
        this.deltaEnd = new Translation2d(center, end);

        this.maxSpeed = maxSpeed;
        extrapolateLookahead = false;
        isLine = false;
        createMotionProfiler(startState, endSpeed);
    }

    public PathSegment(double x1, double y1, double x2, double y2, double cx, double cy, double maxSpeed,
            MotionState startState, double endSpeed, String marker) {
        this.start = new Translation2d(x1, y1);
        this.end = new Translation2d(x2, y2);
        this.center = new Translation2d(cx, cy);

        this.deltaStart = new Translation2d(center, start);
        this.deltaEnd = new Translation2d(center, end);

        this.maxSpeed = maxSpeed;
        extrapolateLookahead = false;
        isLine = false;
        this.marker = marker;
        createMotionProfiler(startState, endSpeed);
    }

    /**
     * @return max speed of the segment
     */
    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void createMotionProfiler(MotionState start_state, double end_speed) {
        MotionProfileConstraints motionConstraints = new MotionProfileConstraints(maxSpeed,
                Constants.kPathFollowingMaxAccel);
        MotionProfileGoal goal_state = new MotionProfileGoal(getLength(), end_speed);
        speedController = MotionProfileGenerator.generateProfile(motionConstraints, goal_state, start_state);
        // System.out.println(speedController);
    }

    /**
     * @return starting point of the segment
     */
    public Translation2d getStart() {
        return start;
    }

    /**
     * @return end point of the segment
     */
    public Translation2d getEnd() {
        return end;
    }

    /**
     * @return the total length of the segment
     */
    public double getLength() {
        if (isLine) {
            return deltaStart.norm();
        } else {
            return deltaStart.norm() * Translation2d.getAngle(deltaStart, deltaEnd).getRadians();
        }
    }

    /**
     * Set whether or not to extrapolate the lookahead point. Should only be true for the last segment in the path
     * 
     * @param val
     */
    public void extrapolateLookahead(boolean val) {
        extrapolateLookahead = val;
    }

    /**
     * Gets the point on the segment closest to the robot
     * 
     * @param position
     *            the current position of the robot
     * @return the point on the segment closest to the robot
     */
    public Translation2d getClosestPoint(Translation2d position) {
        if (isLine) {
            Translation2d delta = new Translation2d(start, end);
            double u = ((position.x() - start.x()) * delta.x() + (position.y() - start.y()) * delta.y())
                    / (delta.x() * delta.x() + delta.y() * delta.y());
            if (u >= 0 && u <= 1)
                return new Translation2d(start.x() + u * delta.x(), start.y() + u * delta.y());
            return (u < 0) ? start : end;
        } else {
            Translation2d deltaPosition = new Translation2d(center, position);
            deltaPosition = deltaPosition.scale(deltaStart.norm() / deltaPosition.norm());
            if (Translation2d.cross(deltaPosition, deltaStart) * Translation2d.cross(deltaPosition, deltaEnd) < 0) {
                return center.translateBy(deltaPosition);
            } else {
                Translation2d startDist = new Translation2d(position, start);
                Translation2d endDist = new Translation2d(position, end);
                return (endDist.norm() < startDist.norm()) ? end : start;
            }
        }
    }

    /**
     * Calculates the point on the segment <code>dist</code> distance from the starting point along the segment.
     * 
     * @param dist
     *            distance from the starting point
     * @return point on the segment <code>dist</code> distance from the starting point
     */
    public Translation2d getPointByDistance(double dist) {
        double length = getLength();
        if (!extrapolateLookahead && dist > length) {
            dist = length;
        }
        if (isLine) {
            return start.translateBy(deltaStart.scale(dist / length));
        } else {
            double deltaAngle = Translation2d.getAngle(deltaStart, deltaEnd).getRadians()
                    * ((Translation2d.cross(deltaStart, deltaEnd) >= 0) ? 1 : -1);
            deltaAngle *= dist / length;
            Translation2d t = deltaStart.rotateBy(Rotation2d.fromRadians(deltaAngle));
            return center.translateBy(t);
        }
    }

    /**
     * Gets the remaining distance left on the segment from point <code>point</code>
     * 
     * @param point
     *            result of <code>getClosestPoint()</code>
     * @return distance remaining
     */
    public double getRemainingDistance(Translation2d position) {
        if (isLine) {
            return new Translation2d(end, position).norm();
        } else {
            Translation2d deltaPosition = new Translation2d(center, position);
            double angle = Translation2d.getAngle(deltaEnd, deltaPosition).getRadians();
            double totalAngle = Translation2d.getAngle(deltaStart, deltaEnd).getRadians();
            return angle / totalAngle * getLength();
        }
    }

    private double getDistanceTravelled(Translation2d robotPosition) {
        Translation2d pathPosition = getClosestPoint(robotPosition);
        double remainingDist = getRemainingDistance(pathPosition);
        return getLength() - remainingDist;

    }

    public double getSpeedByDistance(double dist) {
        if (dist < speedController.startPos()) {
            dist = speedController.startPos();
        } else if (dist > speedController.endPos()) {
            dist = speedController.endPos();
        }
        Optional<MotionState> state = speedController.firstStateByPos(dist);
        if (state.isPresent()) {
            return state.get().vel();
        } else {
            System.out.println("Velocity does not exist at that position!");
            return 0.0;
        }
    }

    public double getSpeedByClosestPoint(Translation2d robotPosition) {
        return getSpeedByDistance(getDistanceTravelled(robotPosition));
    }

    public MotionState getEndState() {
        return speedController.endState();
    }

    public MotionState getStartState() {
        return speedController.startState();
    }

    public String getMarker() {
        return marker;
    }

    public String toString() {
        if (isLine) {
            return "(" + "start: " + start + ", end: " + end + ", speed: " + maxSpeed // + ", profile: " +
                                                                                      // speedController
                    + ")";
        } else {
            return "(" + "start: " + start + ", end: " + end + ", center: " + center + ", speed: " + maxSpeed
                    + ")"; // + ", profile: " + speedController + ")";
        }
    }
}
