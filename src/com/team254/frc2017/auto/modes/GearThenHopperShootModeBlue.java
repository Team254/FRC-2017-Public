package com.team254.frc2017.auto.modes;

import edu.wpi.first.wpilibj.Timer;

import com.team254.frc2017.auto.AutoModeBase;
import com.team254.frc2017.auto.AutoModeEndedException;
import com.team254.frc2017.auto.actions.Action;
import com.team254.frc2017.auto.actions.ActuateHopperAction;
import com.team254.frc2017.auto.actions.BeginShootingAction;
import com.team254.frc2017.auto.actions.CorrectPoseAction;
import com.team254.frc2017.auto.actions.DeployIntakeAction;
import com.team254.frc2017.auto.actions.DrivePathAction;
import com.team254.frc2017.auto.actions.ParallelAction;
import com.team254.frc2017.auto.actions.ResetPoseFromPathAction;
import com.team254.frc2017.auto.actions.ScoreGearAction;
import com.team254.frc2017.auto.actions.SeriesAction;
import com.team254.frc2017.auto.actions.SetFlywheelRPMAction;
import com.team254.frc2017.auto.actions.WaitAction;
import com.team254.frc2017.paths.BoilerGearToHopperBlue;
import com.team254.frc2017.paths.PathContainer;
import com.team254.frc2017.paths.StartToBoilerGearBlue;
import com.team254.frc2017.paths.profiles.PathAdapter;
import com.team254.frc2017.subsystems.Drive;
import com.team254.lib.util.math.RigidTransform2d;

import java.util.Arrays;

/**
 * Scores the preload gear onto the boiler-side peg then deploys the hopper and shoots all 60 balls (10 preload + 50
 * hopper).
 * 
 * This was the primary autonomous mode used at SVR, St. Louis Champs, and FOC.
 * 
 * @see AutoModeBase
 */
public class GearThenHopperShootModeBlue extends AutoModeBase {

    @Override
    protected void routine() throws AutoModeEndedException {
        PathContainer gearPath = new StartToBoilerGearBlue();
        double start = Timer.getFPGATimestamp();
        runAction(new ResetPoseFromPathAction(gearPath));
        runAction(new ParallelAction(Arrays.asList(new Action[] {
                new DrivePathAction(gearPath),
                new ActuateHopperAction(true),
        })));
        runAction(
                new ParallelAction(Arrays.asList(new Action[] {
                        new SetFlywheelRPMAction(2900.0), // spin up flywheel to save time
                        new ScoreGearAction(),
                        new DeployIntakeAction(true)
                })));
        runAction(new CorrectPoseAction(RigidTransform2d.fromTranslation(PathAdapter.getBlueGearCorrection())));
        runAction(new DrivePathAction(new BoilerGearToHopperBlue()));
        System.out.println("Shoot Time: " + (Timer.getFPGATimestamp() - start));
        runAction(new BeginShootingAction());
        runAction(new WaitAction(15));
    }
}
