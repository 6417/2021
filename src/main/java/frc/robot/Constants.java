/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import java.util.HashMap;

import edu.wpi.first.wpilibj.geometry.Translation2d;
import frc.robot.subsystems.Swerve.SwerveModule;
import frc.robot.utilities.MotorInitializer;
import frc.robot.utilities.PIDValues;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants.  This class should not be used for any other purpose.  All constants should be
 * declared globally (i.e. public static).  Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {
    public static final class Joystick{
        public static final int DRIVER_ID = 1;
        public static final int CONTROL_ID = 2;
        public static final int X_BUTTON_ID = 1;
        public static final int A_BUTTON_ID = 2;
        public static final int B_BUTTON_ID = 3;
        public static final int Y_BUTTON_ID = 4;
        public static final int LB_BUTTON_ID = 5;
        public static final int RB_BUTTON_ID = 6;
        public static final int LT_BUTTON_ID = 7;
        public static final int RT_BUTTON_ID = 8;
        public static final int BACK_BUTTON_ID = 9;
        public static final int START_BUTTON_ID = 10;
        public static final int LEFT_JOYSTICK_BUTTON_ID = 11;
        public static final int RIGHT_JOYSTICK_BUTTON_ID = 12;
    }

    public static final class Vision{
        public static final boolean IS_ENABLED = true;
    }

    public static final class SwerveDrive {
        public static enum MountingLocations {
            FrontRight,
            FrontLeft,
            BackRight,
            BackLeft
        }

        public static final boolean enabled = true;
        public static final double zeroingSpeed = 0.0;
        public static final double maxSpeedOfDrive = 0.0; // max velocity for swerve drive in encoder ticks per 100ms
        public static final double maxRotationSpeed = Math.PI; // at full rotation speed the robot will turn by 180 degrees, in rad per second
        public static final HashMap<MountingLocations, SwerveModule.Config> swerveModuleConfigs = new HashMap<>();

        public static SwerveModule.Config commonConfigurations;
        // setting up commmon configurations for all swerve modules
        static {
            commonConfigurations.driveMotorInitializer = null;
            commonConfigurations.rotationMotorInitializer = null;
            commonConfigurations.driveMotorTicksPerRotation = 0.0;
            commonConfigurations.rotationMotorTicksPerRotation = 0.0;
            commonConfigurations.drivePID = new PIDValues(0.0, 0.0, 0.0);
            commonConfigurations.rotationPID = new PIDValues(0.0, 0.0, 0.0);
            commonConfigurations.wheelCircumference = 0.0;
        }

        // adding module specific configurations
        static {
            SwerveModule.Config frontLeftConfig = commonConfigurations.clone();
            frontLeftConfig.mountingPoint = new Translation2d();
            swerveModuleConfigs.put(MountingLocations.FrontLeft, frontLeftConfig);

            SwerveModule.Config frontRightConfig = commonConfigurations.clone();
            frontRightConfig.mountingPoint = new Translation2d();
            swerveModuleConfigs.put(MountingLocations.FrontRight, frontRightConfig);

            SwerveModule.Config backLeftConfig = commonConfigurations.clone();
            backLeftConfig.mountingPoint = new Translation2d();
            swerveModuleConfigs.put(MountingLocations.BackLeft, backLeftConfig);

            SwerveModule.Config backRightConfig = commonConfigurations.clone();
            backRightConfig.mountingPoint = new Translation2d();
            swerveModuleConfigs.put(MountingLocations.BackRight, backRightConfig);
        }
    }
}
