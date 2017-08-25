package com.team254.frc2017.paths.profiles;

/**
 * Interface that holds all the field measurements required by the PathAdapter
 * 
 * @see PathAdapter
 */
public interface FieldProfile {

    public double getRedCenterToBoiler();

    public double getRedWallToAirship();

    public double getRedCenterToHopper();

    public double getRedWallToHopper();

    public double getBlueCenterToBoiler();

    public double getBlueWallToAirship();

    public double getBlueCenterToHopper();

    public double getBlueWallToHopper();

}
