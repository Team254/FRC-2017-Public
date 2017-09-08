# FRC 2017

Team 254's 2017 FRC robot code for Misfire. Misfire's code is written in Java and is based off of WPILib's Java control system.

The code is divided into several packages, each responsible for a different aspect of the robot function. This README explains the function of each package, some of the variable naming conventions used, and setup instructions. Additional information about each specific class can be found in that class's java file.

## How to Write code in IntelliJ
- Create a new directory to be the "top level" of your IntelliJ project. I call mine `~/pofs/robot`
- Check out this repo into that directory:
```
~ $ cd ~/pofs/robot/
~/pofs/robot $ git clone https://github.com/Team254/FRC-2017.git
~/pofs/robot $ ls
FRC-2017
```
- In IntelliJ, create a new empty project (not a java project, just an empty project) at your "top level". This Should create a `.idea` folder if you did it in the right spot
```
# after making the project:
~/pofs/robot $ ls -a
.  ..  .idea  FRC-2017
```
- In intelliJ, you should be in the "project settings" window. Create a new module from existing sources on the `FRC-2017` folder, IntelliJ should pick up `src/` as the content root.
- In the project settings window, add a new Java library for wiplib. Select all the jars in `~/wpilib/java/current/libs/`. It'll give it a wonky name like "networktables", but that doesn't matter. Choose to include it in the `FRC-2017` project.
- In project settings, under the "project section" set your JDK to Java 1.8
- You can now write code with auto-complete in IntelliJ, but not build/deploy
- In IntelliJ, in the "ant build" pane, add `FRC-2017/build.xml`. To deploy code to the robot, double click `athena-project-build.build`

## Code Highlights
- Lag-compensating auto targeting code

	To compensate for camera latency, the [RobotState](src/com/team254/frc2017/RobotState.java) class keeps track of the robot's pose over time and uses the robot's pose at the time the frame was captured rather than the time the frame was recieved to calculate angle displacement.  The [Drive](src/com/team254/frc2017/subsystems/Drive.java) subsystem then takes the aiming parameters calculated by RobotState and uses Motion Magic to turn to the correct heading.   

- Path following with an adaptive pure pursuit controller and motion profiling

	To control autonomous driving, the robot utilizes an [adaptive pure pursuit controller](src/com/team254/lib/util/control/AdaptivePurePursuitController.java) to control steering and a [custom trapezoidal motion profile generator](src/com/team254/lib/util/motion) to control velocity.  

- Path generation and visualization via JavaScript app

	[Cheesy path](cheesy_path), an external JavaScript app. is used for fast and simple path creation.  The app allows a user to create, visualize, export, and import autonomous paths.

- Self-test modes for each subsystem

	Each subsystem contains a [checkSystem()](src/com/team254/frc2017/subsystems/Drive.java#L742) method that tests motor output currents and RPMs.  These self tests allow us to quickly verify that all motors are working properly.

- Automatic path adjustments based on field measurements

	The [PathAdapter](src/com/team254/frc2017/paths/profiles/PathAdapter.java) class enables easy autonomous path recalculation based on field measurements.  The PathAdapter uses field measurements to calculate the waypoint positions used in the [GearThenHopperShoot](src/com/team254/frc2017/auto/modes/GearThenHopperShootModeRed.java) autonomous modes.

- Android-based Vision System for target detection

	[Cheezdroid](vision_app), an android-based vision processing system, is used for image processing and target detection.  The app runs on a Nexus 5 phone.  Since all processing is done on the Nexus 5 itself, the app is able to maintain a smooth 30 FPS output for maximum performance.

## Package Functions
- com.team254.frc2017

	Contains the robot's central functions and holds a file with all numerical constants used throughout the code. For example, the Robot member class controls all routines depending on the robot state.

- com.team254.frc2017.auto

	Handles the excecution of autonomous routines.  Also contains the auto actions and auto modes packages..
	
- com.team254.frc2017.auto.actions

	Contains all actions used during the autonomous period, which all share a common interface, Action (also in this package). Examples include deploying the intake, driving a path, or scoring a gear. Routines interact with the Subsystems, which interact with the hardware.
	
- com.team254.frc2017.auto.modes
	
	Contains all autonomous modes. Autonomous modes consist of a list of autonomous actions excecuted in a certain order.
	
- com.team254.frc2017.loops

	Loops are routines that run periodically on the robot, such as calculating robot pose, processing vision feedback, or updating subsystems. All Loops implement the Loop interface and are handled (started, stopped, added) by the Looper class, which runs att 200 Hz.
	The Robot class has one main Looper, mEnabledLooper, that runs all loops when the robot is enabled.
	
- com.team254.frc2017.paths

	Contains all paths that the robot drives during autonomous mode.  Each path is made up of a list of Waypoints.  The PathBuilder class, which is also included in this package, transforms these waypoints into a series of arcs and line segments.
	
- com.team254.frc2017.paths.profiles

	Contains a set of field and robot profiles.  Field profiles contain field measurements that are read by the PathAdapter class, which then builds a set of paths to match a specific field's measurements.  Robot profiles contain robot driving error measurements.  Due to the simplified nature of our kinematics model, the robot often calculates that it is in a different position than it actually is.  Robot profiles help the robot compensate for this error between actual position and calculated position.
	
- com.team254.frc2017.subsystems
	
	Subsystems are consolidated into one central class per subsystem, all of which implement the Subsystem abstract class. Each Subsystem uses state machines for control.
	Each Subsystem is also a singleton, meaning that there is only one instance of each Subsystem class. To modify a subsystem, one would get the instance of the subsystem and change its desired state. The Subsystem class will work on setting the desired state.
	
- com.team254.frc2017.vision

	Handles the Android vision tracking system. This includes handling all ADB (Android Debug Bridge) communications with the phone and creating VisionUpdate data with the data from the phone.
	VisionUpdates consist of TargetInfo objects (the target's coordinates in 3D space), a timestamp, and a "valid" value (if the update is valid). This represents the target data from each frame processed by the phone.
	The VisionServer class unifies the vision system. Like the Subsystems, there is only one instance of the VisionServer.

- com.team254.frc2017.vision.messages

	Contains messages used by the vision system: a heartbeat signal that's regularly sent out and a "camera mode" message that contains information about the camera's state.
	All Messages implement the VisionMessage abstract class.
	
- com.team254.lib.util

	A collection of assorted utilities classes used in the robot code. This includes custom classes for hardware devices (encoders, gyroscopes, etc.) as well as mathematical helper functions, especially regarding translations and rotations. Check each .java file for more information.
	
- com.team254.lib.util.control

	Contains all the classes used for the robot's steering controller during autonomous driving.  The robot uses an adaptive pure pursuit controller to control steering.  You can read about it [here](https://www.mathworks.com/help/robotics/ug/pure-pursuit-controller.html)
	
- com.team254.lib.util.drivers

	Contains a set of custom classes for hardware devices (pressure sensors, ultrasonic sensors, NavX board, etc.)

- com.team254.lib.util.math

	Contains a set of helper classes for mathmatical calculations.

- com.team254.lib.util.control

	Contains all motion profiling code used for autonomous driving.  We use trapezoidal motion profiles for smooth acceleration and minimal slip.
	
	
## Variable Naming Conventions

- k_*** (i.e. kTrackWidthInches)    : Final constants, especialy those found in the Constants.java file
- K_*** (i.e. K_VISION_MODE)    : Static constants
- m***  (i.e. mIsHighGear): Private instance variables
