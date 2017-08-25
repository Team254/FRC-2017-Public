package com.team254.frc2017.auto.modes;

import edu.wpi.first.wpilibj.Timer;

import com.team254.frc2017.auto.AutoModeBase;
import com.team254.frc2017.auto.AutoModeEndedException;
import com.team254.frc2017.auto.actions.Action;
import com.team254.frc2017.auto.actions.BeginShootingAction;
import com.team254.frc2017.auto.actions.DeployIntakeAction;
import com.team254.frc2017.auto.actions.DrivePathAction;
import com.team254.frc2017.auto.actions.ForceEndPathAction;
import com.team254.frc2017.auto.actions.ParallelAction;
import com.team254.frc2017.auto.actions.PrintDebugAction;
import com.team254.frc2017.auto.actions.ResetPoseFromPathAction;
import com.team254.frc2017.auto.actions.SeriesAction;
import com.team254.frc2017.auto.actions.SetFlywheelRPMAction;
import com.team254.frc2017.auto.actions.TurnUntilSeesTargetAction;
import com.team254.frc2017.auto.actions.WaitAction;
import com.team254.frc2017.auto.actions.WaitForPathMarkerAction;
import com.team254.frc2017.paths.PathContainer;
import com.team254.frc2017.paths.StartToHopperRed;
import com.team254.frc2017.subsystems.Drive;
import com.team254.lib.util.DriveSignal;
import com.team254.lib.util.math.Rotation2d;

import java.util.Arrays;

/**
 * Rams the field hopper head on with the robot's intake then waits for the balls to fall into the robot and shoots
 * them.
 * 
 * @see AutoModeBase
 */
public class RamHopperShootModeRed extends AutoModeBase {

    @Override
    protected void routine() throws AutoModeEndedException {
        PathContainer hopperPath = new StartToHopperRed();
        runAction(new ResetPoseFromPathAction(hopperPath));
        double startTime = Timer.getFPGATimestamp();
        runAction(new DeployIntakeAction(true));
        runAction(
                new ParallelAction(Arrays.asList(new Action[] {
                        new DrivePathAction(hopperPath),
                        new SeriesAction(Arrays.asList(new Action[] {
                                new WaitForPathMarkerAction("RamWall"), new PrintDebugAction("RamWall"),
                                new WaitAction(0.25), new ForceEndPathAction()
                        }))
                }))); // Drive to hopper, cancel path once the robot runs into the wall
        runAction(new SetFlywheelRPMAction(3100));
        runAction(new WaitAction(2.6)); // wait for balls
        Drive.getInstance().setOpenLoop(new DriveSignal(-1, -1));
        runAction(new WaitAction(0.2));
        runAction(new TurnUntilSeesTargetAction(Rotation2d.fromDegrees(200)));
        System.out.println("Time to shoot: " + (Timer.getFPGATimestamp() - startTime));
        runAction(new BeginShootingAction()); // aim + fire
        runAction(new WaitAction(20)); // keep firing until auto ends
    }
}
