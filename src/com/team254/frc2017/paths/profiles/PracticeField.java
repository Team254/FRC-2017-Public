package com.team254.frc2017.paths.profiles;

/**
 * Contains the measurements for the practice field at the 254 lab
 */
public class PracticeField implements FieldProfile {

    @Override
    public double getRedCenterToBoiler() {
        return 127.5;
    }

    @Override
    public double getRedWallToAirship() {
        return 116.5;
    }

    @Override
    public double getRedCenterToHopper() {
        return 160.66;
    }

    @Override
    public double getRedWallToHopper() {
        return 108.0;
    }

    @Override
    public double getBlueCenterToBoiler() {
        return 125.5;
    }

    @Override
    public double getBlueWallToAirship() {
        return 114.0;
    }

    @Override
    public double getBlueCenterToHopper() {
        return 161.0;
    }

    @Override
    public double getBlueWallToHopper() {
        return 110.0;
    }

}
