package com.team254.frc2017.paths.profiles;

/**
 * Contains the measurements for the Daly field at St. Louis champs
 */
public class DalyField implements FieldProfile {

    @Override
    public double getRedCenterToBoiler() {
        return 125.44;
    }

    @Override
    public double getRedWallToAirship() {
        return 114.5;
    }

    @Override
    public double getRedCenterToHopper() {
        return 162;
    }

    @Override
    public double getRedWallToHopper() { // TODO: verify this
        return 110.5;
    }

    @Override
    public double getBlueCenterToBoiler() {
        return 126.76;
    }

    @Override
    public double getBlueWallToAirship() {
        return 113.75;
    }

    @Override
    public double getBlueCenterToHopper() {
        return 162.25;
    }

    @Override
    public double getBlueWallToHopper() {
        return 109.625;
    }

}
