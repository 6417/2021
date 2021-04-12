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

import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.geometry.Translation2d;
import frc.robot.subsystems.Swerve.SwerveModule;
import frc.robot.utilities.PIDValues;
import frc.robot.utilities.fridolinsMotor.FridoCANSparkMax;
import frc.robot.utilities.fridolinsMotor.FridoTalonSRX;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.LimitSwitchPolarity;
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
            public static final int fieledOriented = Joystick.Y_BUTTON_ID;
            public static final int throwerOriented = Joystick.A_BUTTON_ID;
            public static final int pickupOriented = Joystick.B_BUTTON_ID;
        }

        private static void setSwerveDriveConstants() {
            zeroingSpeed = 0.1;
            maxSpeedOfDrive = 0.293 * Math.PI; // calculated needs to measured
        }

        private static void setSwerveDriveConstantsFor2019Test() {
            zeroingSpeed = 500.0;
            maxSpeedOfDrive = 0.8531647523;
        }

        public static final boolean enabled = true;
        public static final boolean rotateAllModulesInSameDirection = true;
        public static final boolean joystickYinverted = true;
        public static final boolean joystickXinverted = true;
        private static final boolean swerveTestOn2019Robot = false;
        private static final boolean centricSwerve = false;
        public static double zeroingSpeed;
        public static final double deadBand = 0.075;
        public static final double finetuningZeroFactor = 0.1;
        public static double maxSpeedOfDrive; // in meters per second
        public static final double maxRotationSpeed = 15 *  Math.PI / 16; // at full rotation speed the robot will turn by 180
        // degrees, in rad per second
        public static final HashMap<MountingLocations, SwerveModule.Config> swerveModuleConfigs = new HashMap<>();

        public static SwerveLimiter.Config limiterConfig = new SwerveLimiter.Config();
        public static final SwerveLimiter.RotationDirectionCorrectorGetter<MountingLocations> directionCorectorGetter = (
                Map<MountingLocations, SwerveLimiter.ModuleRotationVectors> rotationDirections,
                boolean isRobotRotating) -> SwerveLimiter.getModuleRotaionDirectionCorrections(rotationDirections,
                        isRobotRotating);

        static {
            if (swerveTestOn2019Robot)
                setSwerveDriveConstantsFor2019Test();
            else
                setSwerveDriveConstants();
        }

        // setting up limiter config
        static {
            limiterConfig.clock = System::nanoTime;
            limiterConfig.defaultLoopTime = 20000; // in nano seconds
            limiterConfig.gauseStrechingFactor = -Math.log(Math.PI / 4.59678e8);
            limiterConfig.centricSwerve = centricSwerve;
        }

        public static SwerveModule.Config commonConfigurations = new SwerveModule.Config();
        public static double defaultSpeedFactor = 1.0;
        // setting up commmon configurations for all swerve modules
        static {
            if (swerveTestOn2019Robot)
                addCommonModuleConfigurarionsFor2019Test();
            else
                addCommonModuleConfigurarions();
        }

        // adding module specific configurations
        static {
            if (swerveTestOn2019Robot)
                addModuleSpecificConfigurationsFor2019Test();
            else
                addModuleSpecificConfigurarions();
        }

        private static void addCommonModuleConfigurarions() {
            commonConfigurations.driveMotorTicksPerRotation = 5800 / 3;
            commonConfigurations.rotationMotorTicksPerRotation = 36.0;
            commonConfigurations.drivePID = new PIDValues(0.00001, 0.0, 0.0, 0.000166);
            commonConfigurations.rotationPID = new PIDValues(0.3959, 0.0, 0.2);
            commonConfigurations.wheelCircumference = 0.09767 * Math.PI;
            commonConfigurations.limiterInitializer = () -> new SwerveLimiter(limiterConfig);
            commonConfigurations.maxVelocity = maxSpeedOfDrive;
            commonConfigurations.centricSwerve = centricSwerve;
            commonConfigurations.driveEncoderType = FridolinsMotor.FeedbackDevice.CANEncoder;
            commonConfigurations.rotationEncoderType = FridolinsMotor.FeedbackDevice.CANEncoder;
            commonConfigurations.limitModuleStates = false;
            commonConfigurations.limitSwitchPolarity = LimitSwitchPolarity.kNormallyOpen;
        }

        private static FridolinsMotor motorInitializer(int id, MotorType motorType) {
            FridolinsMotor motor = new FridoCANSparkMax(id, motorType);
            motor.factoryDefault();
            return motor;
        }

        private static void addModuleSpecificConfigurarions() {
            SwerveModule.Config frontLeftConfig = commonConfigurations.clone();
            frontLeftConfig.mountingPoint = new Translation2d(0.2725, 0.139);
            frontLeftConfig.driveMotorInitializer = () -> motorInitializer(32, MotorType.kBrushless);
            frontLeftConfig.rotationMotorInitializer = () -> motorInitializer(33, MotorType.kBrushless);
            frontLeftConfig.driveMotorInverted = false;
            frontLeftConfig.halSensorPosition = 8.952413559 + 9.0;
            swerveModuleConfigs.put(MountingLocations.FrontLeft, frontLeftConfig);

            SwerveModule.Config frontRightConfig = commonConfigurations.clone();
            frontRightConfig.mountingPoint = new Translation2d(-0.2725, 0.139);
            frontRightConfig.driveMotorInitializer = () -> motorInitializer(30, MotorType.kBrushless);
            frontRightConfig.rotationMotorInitializer = () -> motorInitializer(31, MotorType.kBrushless);
            frontRightConfig.driveMotorInverted = false;
            frontRightConfig.halSensorPosition = 8.785744667 + 9.0;            
            swerveModuleConfigs.put(MountingLocations.FrontRight, frontRightConfig);

            SwerveModule.Config backLeftConfig = commonConfigurations.clone();
            backLeftConfig.mountingPoint = new Translation2d(0.2725, -0.139);
            backLeftConfig.driveMotorInitializer = () -> motorInitializer(36, MotorType.kBrushless);
            backLeftConfig.rotationMotorInitializer = () -> motorInitializer(37, MotorType.kBrushless);
            backLeftConfig.driveMotorInverted = false;
            backLeftConfig.halSensorPosition = 8.904793739 + 9.0;
            swerveModuleConfigs.put(MountingLocations.BackLeft, backLeftConfig);

            SwerveModule.Config backRightConfig = commonConfigurations.clone();
            backRightConfig.mountingPoint = new Translation2d(-0.2725, -0.139);
            backRightConfig.driveMotorInitializer = () -> motorInitializer(34, MotorType.kBrushless);
            backRightConfig.rotationMotorInitializer = () -> motorInitializer(35, MotorType.kBrushless);
            backRightConfig.driveMotorInverted = false;
            backRightConfig.halSensorPosition = 8.952413559 + 9.0;
            swerveModuleConfigs.put(MountingLocations.BackRight, backRightConfig);
        }

        private static void addCommonModuleConfigurarionsFor2019Test() {
            commonConfigurations.driveMotorTicksPerRotation = 11564.0;
            commonConfigurations.rotationMotorTicksPerRotation = 196608.0;
            commonConfigurations.drivePID = new PIDValues(0.015, 0.0, 0.0, 0.03375);
            commonConfigurations.drivePID.slotIdX = Optional.of(0);
            commonConfigurations.rotationPID = new PIDValues(0.6, 0.16, 4.0);
            commonConfigurations.rotationPID.slotIdX = Optional.of(0);
            commonConfigurations.wheelCircumference = 0.1 * Math.PI;
            commonConfigurations.limiterInitializer = () -> new SwerveLimiter(limiterConfig);
            commonConfigurations.maxVelocity = maxSpeedOfDrive;
            commonConfigurations.driveEncoderType = FridolinsMotor.FeedbackDevice.QuadEncoder;
            commonConfigurations.rotationEncoderType = FridolinsMotor.FeedbackDevice.QuadEncoder;
            commonConfigurations.centricSwerve = centricSwerve;
            commonConfigurations.limitModuleStates = true;
        }

        private static void addModuleSpecificConfigurationsFor2019Test() {
            SwerveModule.Config frontLeftConfig = commonConfigurations.clone();
            frontLeftConfig.mountingPoint = new Translation2d(0.32, 0.305);
            frontLeftConfig.driveMotorInitializer = () -> new FridoTalonSRX(32);
            frontLeftConfig.rotationMotorInitializer = () -> new FridoTalonSRX(33);
            frontLeftConfig.driveMotorInverted = true;
            frontLeftConfig.driveSensorInverted = Optional.of(true);
            frontLeftConfig.halSensorPosition = 196608.0;
            swerveModuleConfigs.put(MountingLocations.FrontLeft, frontLeftConfig);

            SwerveModule.Config frontRightConfig = commonConfigurations.clone();
            frontRightConfig.mountingPoint = new Translation2d(-0.32, 0.305);
            frontRightConfig.driveMotorInitializer = () -> new FridoTalonSRX(38);
            frontRightConfig.rotationMotorInitializer = () -> new FridoTalonSRX(39);
            frontRightConfig.driveMotorInverted = true;
            frontRightConfig.driveSensorInverted = Optional.of(true);
            frontRightConfig.halSensorPosition = 196608.0;
            swerveModuleConfigs.put(MountingLocations.FrontRight, frontRightConfig);

            SwerveModule.Config backLeftConfig = commonConfigurations.clone();
            backLeftConfig.mountingPoint = new Translation2d(0.32, -0.305);
            backLeftConfig.driveMotorInitializer = () -> new FridoTalonSRX(34);
            backLeftConfig.rotationMotorInitializer = () -> new FridoTalonSRX(35);
            backLeftConfig.driveMotorInverted = true;
            backLeftConfig.driveSensorInverted = Optional.of(true);
            backLeftConfig.halSensorPosition = 196608.0;
            swerveModuleConfigs.put(MountingLocations.BackLeft, backLeftConfig);

            SwerveModule.Config backRightConfig = commonConfigurations.clone();
            backRightConfig.mountingPoint = new Translation2d(-0.32, -0.305);
            backRightConfig.driveMotorInitializer = () -> new FridoTalonSRX(36);
            backRightConfig.rotationMotorInitializer = () -> new FridoTalonSRX(37);
            backRightConfig.driveMotorInverted = false;
            backRightConfig.driveSensorInverted = Optional.of(true);
            backRightConfig.halSensorPosition = 196608.0;
            swerveModuleConfigs.put(MountingLocations.BackRight, backRightConfig);
        }
    }
}
