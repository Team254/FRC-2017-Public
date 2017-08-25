package com.team254.frc2017.auto.modes;

import edu.wpi.first.wpilibj.DriverStation;

import com.team254.frc2017.auto.AutoModeBase;
import com.team254.frc2017.auto.AutoModeEndedException;

/**
 * Auto detect which alliance we are part of then run the GearThenHopperShoot auto. Default to RED side.
 * 
 * @see AutoModeBase
 * @see GearThenHopperShootModeBlue
 * @see GearThenHopperShootModeRed
 */
public class AutoDetectAllianceGearThenShootMode extends AutoModeBase {
    @Override
    protected void routine() throws AutoModeEndedException {
        AutoModeBase selectedAutoMode = new GearThenHopperShootModeRed();
        boolean isRed = true;
        DriverStation.Alliance alliance = DriverStation.getInstance().getAlliance();

        if (alliance == DriverStation.Alliance.Blue) {
            selectedAutoMode = new GearThenHopperShootModeBlue();
        } else if (alliance == DriverStation.Alliance.Invalid) {
            System.out.println("INVALID ALLIANCE.");
        }

        System.out.print("Attempting to detect alliance side: ");
        if (isRed) {
            System.out.println("Red.");
        } else {
            System.out.println("Blue.");
        }
        selectedAutoMode.run();
    }
}
