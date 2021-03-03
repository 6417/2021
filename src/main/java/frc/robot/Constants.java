/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import edu.wpi.first.wpilibj.geometry.Translation2d;
import frc.robot.subsystems.Swerve.SwerveModule;
import frc.robot.utilities.PIDValues;
import frc.robot.utilities.fridolinsMotor.FridoTalonSRX;
import frc.robot.utilities.swerveLimiter.SwerveLimiter;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide
 * numerical or boolean constants. This class should not be used for any other
 * purpose. All constants should be declared globally (i.e. public static). Do
 * not put anything functional in this class.
 *
 * <p>
 * It is advised to statically import this class (or one of its inner classes)
 * wherever the constants are needed, to reduce verbosity.
 */
public final class Constants {
    public static final class Joystick {
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

    public static final class Vision {
        public static final boolean IS_ENABLED = false;
    }

    public static final class SwerveDrive {
        public static enum MountingLocations {
            FrontRight, FrontLeft, BackRight, BackLeft
        }

        public static final class ButtounIds {
            public static final int zeroEncoders = Joystick.X_BUTTON_ID;
        }
        
        public static final boolean enabled = true;
        public static final double zeroingSpeed = 0.3;
        public static final double maxSpeedOfDrive = 14132.0; // max velocity for swerve drive in encoder ticks per 100ms
        public static final double maxRotationSpeed = Math.PI; // at full rotation speed the robot will turn by 180
                                                               // degrees, in rad per second
        public static final HashMap<MountingLocations, SwerveModule.Config> swerveModuleConfigs = new HashMap<>();

        public static SwerveLimiter.Config limiterConfig = new SwerveLimiter.Config();
        public static final SwerveLimiter.RotationDirectionCorrectorGetter<MountingLocations> directionCorectorGetter = (
                Map<MountingLocations, SwerveLimiter.ModuleRotationVectors> rotationDirections,
                boolean isRobotRotating) -> SwerveLimiter.getModuleRotaionDirectionCorrections(rotationDirections,
                        isRobotRotating);

        // setting up limiter config
        static {
            limiterConfig.clock = System::nanoTime;
            limiterConfig.defaultLoopTime = 20000; // in nano seconds
            limiterConfig.gauseStrechingFactor = -Math.log(Math.PI / 4.59678e8);
        }

        public static SwerveModule.Config commonConfigurations = new SwerveModule.Config();
        // setting up commmon configurations for all swerve modules
        static {
            commonConfigurations.driveMotorTicksPerRotation = 196608.0;
            commonConfigurations.rotationMotorTicksPerRotation = 11564.0;
            commonConfigurations.drivePID = new PIDValues(0.015, 0.0, 0.0, 0.03375);
            commonConfigurations.drivePID.slotIdX = Optional.of(0);
            commonConfigurations.rotationPID = new PIDValues(0.6, 0.16, 4.0);
            commonConfigurations.rotationPID.slotIdX = Optional.of(0);
            commonConfigurations.wheelCircumference = 0.1 * Math.PI;
            commonConfigurations.limiterInitializer = () -> new SwerveLimiter(limiterConfig);
            commonConfigurations.maxVelocity = maxSpeedOfDrive;
        }

        // adding module specific configurations
        static {
            SwerveModule.Config frontLeftConfig = commonConfigurations.clone();
            frontLeftConfig.mountingPoint = new Translation2d(0.32, -0.305);
            frontLeftConfig.driveMotorInitializer = () -> new FridoTalonSRX(32);
            frontLeftConfig.rotationMotorInitializer = () -> new FridoTalonSRX(33);
            swerveModuleConfigs.put(MountingLocations.FrontLeft, frontLeftConfig);

            SwerveModule.Config frontRightConfig = commonConfigurations.clone();
            frontRightConfig.mountingPoint = new Translation2d(0.32, 0.305);
            frontRightConfig.driveMotorInitializer = () -> new FridoTalonSRX(38);
            frontRightConfig.rotationMotorInitializer = () -> new FridoTalonSRX(39);
            swerveModuleConfigs.put(MountingLocations.FrontRight, frontRightConfig);

            SwerveModule.Config backLeftConfig = commonConfigurations.clone();
            backLeftConfig.mountingPoint = new Translation2d(-0.32, -0.305);
            backLeftConfig.driveMotorInitializer = () -> new FridoTalonSRX(34);
            backLeftConfig.rotationMotorInitializer = () -> new FridoTalonSRX(35);
            swerveModuleConfigs.put(MountingLocations.BackLeft, backLeftConfig);

            SwerveModule.Config backRightConfig = commonConfigurations.clone();
            backRightConfig.mountingPoint = new Translation2d(-0.32, 0.305);
            backRightConfig.driveMotorInitializer = () -> new FridoTalonSRX(36);
            backRightConfig.rotationMotorInitializer = () -> new FridoTalonSRX(37);
            swerveModuleConfigs.put(MountingLocations.BackRight, backRightConfig);
        }
    }
}
