package com.team254.frc2017.paths.profiles;

/**
 * Contains the corrective values for Comp bot
 */
public class CompBot implements RobotProfile {

    @Override
    public double getRedBoilerGearXCorrection() {
        return 3.0;
    }

    @Override
    public double getRedBoilerGearYCorrection() {
        return 4.0;
    }

    @Override
    public double getRedHopperXOffset() {
        return 0.0;
    }

    @Override
    public double getRedHopperYOffset() {
        return -3.0;
    }

    @Override
    public double getBlueBoilerGearXCorrection() {
        return 0.5;
    }

    @Override
    public double getBlueBoilerGearYCorrection() {
        return 1.0;
    }

    @Override
    public double getBlueHopperXOffset() {
        return -5.5;
    }

    @Override
    public double getBlueHopperYOffset() {
        return 0.0;
    }

}
