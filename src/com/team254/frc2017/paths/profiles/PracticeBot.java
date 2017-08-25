package com.team254.frc2017.paths.profiles;

/**
 * Contains the corrective values for Practice bot
 */
public class PracticeBot implements RobotProfile {

    @Override
    public double getRedBoilerGearXCorrection() {
        return 2.5;
    }

    @Override
    public double getRedBoilerGearYCorrection() {
        return 7.0;
    }

    @Override
    public double getRedHopperXOffset() {
        return 0.0;
    }

    @Override
    public double getRedHopperYOffset() {
        return -6.0;
    }

    @Override
    public double getBlueBoilerGearXCorrection() {
        return 2.5;
    }

    @Override
    public double getBlueBoilerGearYCorrection() {
        return -1.0;
    }

    @Override
    public double getBlueHopperXOffset() {
        return 0.0;
    }

    @Override
    public double getBlueHopperYOffset() {
        return -4.0;
    }

}
