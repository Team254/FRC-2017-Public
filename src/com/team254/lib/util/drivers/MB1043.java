package com.team254.lib.util.drivers;

/**
 * Driver for a MB1043 ultrasonic sensor
 */
public class MB1043 extends UltrasonicSensor {
    public MB1043(int port) {
        super(port);
        this.mScalingFactor = 1024.0; // Per 1 mm (Vcc/1024 per 5mm, Vcc = 5v)
    }

    public double getAverageDistanceInches() {
        return getAverageDistance() * 0.0393701; // Inches per mm
    }

    public double getRawDistanceInches() {
        return super.getLatestDistance() * 0.0393701; // Inches per mm;
    }
}
