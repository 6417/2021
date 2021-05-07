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
import java.util.function.Supplier;

import com.revrobotics.CANSparkMax.SoftLimitDirection;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.geometry.Translation2d;
import frc.robot.subsystems.swerve.SwerveModule;
import frc.robot.utilities.PIDValues;
import frc.robot.utilities.Vector2d;
import frc.robot.utilities.fridolinsMotor.FridoCANSparkMax;
import frc.robot.utilities.fridolinsMotor.FridoTalon;
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

public class Constants {
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

    public static final int zeroNavxButtonID = Joystick.BACK_BUTTON_ID;

    public static final class Vision {
        public static final boolean IS_ENABLED = true;
    }

    public static class SwerveDrive {
        public static enum MountingLocations {
            FrontRight, FrontLeft, BackRight, BackLeft
        }

        public static class ButtounIds extends DriveButtonIds {
            public static final int zeroEncoders = Joystick.START_BUTTON_ID;
            public static final int breakButton = Joystick.RT_BUTTON_ID;
            public static final int centircSwerveMode = Joystick.LT_BUTTON_ID;
            public static final int fullSpeed = Joystick.LB_BUTTON_ID;
        }

        private static void setSwerveDriveConstants() {
            zeroingSpeed = 0.1;
            maxFineTuneOffsetForZeroEncodersCommand = 1.0;
            maxSpeedOfDrive = 0.6981;
        }

        private static void setUpLimiterConfig() {
            limiterConfig.clock = System::nanoTime;
            limiterConfig.defaultLoopTime = 20000; // in nano seconds
            limiterConfig.gauseXStrechingFactor = Math.sqrt(Math.log(10000 / (Math.PI + 7500)));
            limiterConfig.gauseYOffset = 1.975;
            SwerveLimiter.rotationDirectionInversionTolerance = 0.9;
        }

        public static final boolean enabled = false && driveEnabled;
        public static final boolean rotateAllModulesInSameDirection = false;
        public static final boolean joystickYinverted = true;
        public static final boolean joystickXinverted = true;
        public static double zeroingSpeed;
        public static final double deadBand = 0.075;
        public static final int limiterYOffsetMovingAverageHistorySize = 20;
        public static final double yOffsetMapperMaxVoltage = 12.5;
        public static final double yOffsetMapperMinVoltage = 9;
        public static final double finetuningZeroFactor = 0.1;
        public static double maxFineTuneOffsetForZeroEncodersCommand;
        public static double maxSpeedOfDrive; // in meters per second
        public static final double maxRotationSpeed = 15 * Math.PI / 16; // at full rotation speed the robot will turn
                                                                         // by 180 degrees, in rad per second
        public static final Map<MountingLocations, SwerveModule.Config> swerveModuleConfigs = new HashMap<>();

        public static SwerveLimiter.Config limiterConfig = new SwerveLimiter.Config();
        public static final SwerveLimiter.RotationDirectionCorrectorGetter<MountingLocations> directionCorectorGetter = (
                Map<MountingLocations, SwerveLimiter.ModuleRotationVectors> rotationDirections,
                boolean isRobotRotating) -> SwerveLimiter.getModuleRotaionDirectionCorrections(rotationDirections,
                        isRobotRotating);
        public static SwerveModule.Config commonConfigurations = new SwerveModule.Config();
        public static double defaultSpeedFactor = 0.75;
        public static double slowSpeedFactor = 0.35;

        static {
            setSwerveDriveConstants();
            setUpLimiterConfig();
            addCommonModuleConfigurarions();
            addModuleSpecificConfigurarions();
        }

        private static void addCommonModuleConfigurarions() {
            commonConfigurations.driveMotorTicksPerRotation = 5800 / 3;
            commonConfigurations.rotationMotorTicksPerRotation = 36.0;
            commonConfigurations.drivePID = new PIDValues(0.00001, 0.0, 0.0, 0.0002);
            commonConfigurations.drivePID.slotIdX = Optional.of(0);
            commonConfigurations.drivePID.setAcceleration(0.0000001);
            commonConfigurations.rotationPID = new PIDValues(0.3959, 0.0, 0.2);
            commonConfigurations.wheelCircumference = 0.09767 * Math.PI;
            commonConfigurations.limiterInitializer = () -> new SwerveLimiter(limiterConfig);
            commonConfigurations.maxVelocity = maxSpeedOfDrive;
            commonConfigurations.driveEncoderType = FridolinsMotor.FeedbackDevice.CANEncoder;
            commonConfigurations.rotationEncoderType = FridolinsMotor.FeedbackDevice.CANEncoder;
            commonConfigurations.limitModuleStates = false;
            commonConfigurations.limitSwitchPolarity = LimitSwitchPolarity.kNormallyOpen;
            commonConfigurations.driveAccelerationForward = 2000;
            commonConfigurations.driveAccelerationSideWays = 500;
            commonConfigurations.problemDirectionsWhileBreaking = new Vector2d[] {
                    new Vector2d(-1 / Math.sqrt(2), -1 / Math.sqrt(2)),
                    new Vector2d(-1 / Math.sqrt(2), 1 / Math.sqrt(2)) };
            commonConfigurations.problemDirectionsBreakModeGauseStrechingFactor = 1.0;
        }

        private static FridoCANSparkMax angleMotorInitializer(int id, MotorType motorType) {
            FridoCANSparkMax motor = new FridoCANSparkMax(id, motorType);
            motor.factoryDefault();
            motor.enableVoltageCompensation(10.4);
            return motor;
        }

        private static FridolinsMotor driveMotorInitializer(int id, MotorType motorType) {
            FridoCANSparkMax motor = angleMotorInitializer(id, motorType);
            motor.enableForwardLimitSwitch(LimitSwitchPolarity.kDisabled, false);
            motor.enableReverseLimitSwitch(LimitSwitchPolarity.kDisabled, false);
            motor.enableSoftLimit(SoftLimitDirection.kForward, false);
            motor.enableSoftLimit(SoftLimitDirection.kReverse, false);
            return motor;
        }

        private static void addModuleSpecificConfigurarions() {
            SwerveModule.Config frontLeftConfig = commonConfigurations.clone();
            frontLeftConfig.mountingPoint = new Translation2d(0.139, 0.2725);
            frontLeftConfig.driveMotorInitializer = () -> driveMotorInitializer(32, MotorType.kBrushless);
            frontLeftConfig.rotationMotorInitializer = () -> angleMotorInitializer(33, MotorType.kBrushless);
            frontLeftConfig.driveMotorInverted = false;
            frontLeftConfig.halSensorPosition = 8.952413559 + 9.0;
            swerveModuleConfigs.put(MountingLocations.FrontLeft, frontLeftConfig);

            SwerveModule.Config frontRightConfig = commonConfigurations.clone();
            frontRightConfig.mountingPoint = new Translation2d(-0.139, 0.2725);
            frontRightConfig.driveMotorInitializer = () -> driveMotorInitializer(30, MotorType.kBrushless);
            frontRightConfig.rotationMotorInitializer = () -> angleMotorInitializer(31, MotorType.kBrushless);
            frontRightConfig.driveMotorInverted = false;
            frontRightConfig.halSensorPosition = 8.785744667 + 9.0;
            swerveModuleConfigs.put(MountingLocations.FrontRight, frontRightConfig);

            SwerveModule.Config backLeftConfig = commonConfigurations.clone();
            backLeftConfig.mountingPoint = new Translation2d(0.139, -0.2725);
            backLeftConfig.driveMotorInitializer = () -> driveMotorInitializer(36, MotorType.kBrushless);
            backLeftConfig.rotationMotorInitializer = () -> angleMotorInitializer(37, MotorType.kBrushless);
            backLeftConfig.driveMotorInverted = false;
            backLeftConfig.halSensorPosition = 8.904793739 + 9.0;
            swerveModuleConfigs.put(MountingLocations.BackLeft, backLeftConfig);

            SwerveModule.Config backRightConfig = commonConfigurations.clone();
            backRightConfig.mountingPoint = new Translation2d(-0.139, -0.2725);
            backRightConfig.driveMotorInitializer = () -> driveMotorInitializer(34, MotorType.kBrushless);
            backRightConfig.rotationMotorInitializer = () -> angleMotorInitializer(35, MotorType.kBrushless);
            backRightConfig.driveMotorInverted = false;
            backRightConfig.halSensorPosition = 8.952413559 + 9.0;
            swerveModuleConfigs.put(MountingLocations.BackRight, backRightConfig);
        }

    }

    public static class BallPickUp {
        public static boolean isEnabled = true;

        // MotorIDs
        public static final int pickUpMotor_ID = 12;
        public static final int tunnelMotor_ID = 10;

        // MotorSuppliers
        public static Supplier<FridolinsMotor> pickUpMotor = () -> new FridoCANSparkMax(pickUpMotor_ID,
                MotorType.kBrushless);
        public static Supplier<FridolinsMotor> tunnelMotor = () -> new FridoCANSparkMax(tunnelMotor_ID,
                MotorType.kBrushless);
        public static final boolean tunnelMotorInvertation = true;

        // variables to find out the ballcolor
        public static final int comparativeValueBlueLow = 105;
        public static final int comparativeValueBlueHigh = 50;
        public static final int comparativeValueRedLow = 110;
        public static final int comparativeValueRedHigh = 60;

        public static final double ticksForTunnelMotor = 0;

        public static final int countsPerRevTunnelMotor = 1;
        public static final int countsPerRevPickUpMotor = 1;

        public static final double pickUpSpeed = 1; // TODO optimal speed
        public static final double tunnelMotorPickUpSpeed = 0.4; // TODO
        public static final double loadSpeed = 1; // TODO optimal speed
        public static final double releaseSpeed = 1; // TODO optimal speed

        public static final boolean isLightBarrierInverted = true;
    }

    public static class DriveButtonIds {
        public static final int fieledOriented = Joystick.Y_BUTTON_ID;
        public static final int throwerOriented = Joystick.A_BUTTON_ID;
        public static final int pickupOriented = Joystick.B_BUTTON_ID;
        public static final int slowSpeedMode = Joystick.RB_BUTTON_ID;
    }

    public static final boolean driveEnabled = true;

    public static class MecanumDrive {
        public static class ButtonIds extends DriveButtonIds {

        }

        public static final double slowModeSpeedFactor = 0.4;

        public static final boolean IS_ENABLED = true && driveEnabled;
        public static final double SECONDS_TO_ACCELERATE = 0.125;

        public static final Supplier<FridolinsMotor> frontRightMotorInitializer = () -> new FridoCANSparkMax(31, MotorType.kBrushed);
        public static final Supplier<FridolinsMotor> backRightMotorInitializer = () -> new FridoCANSparkMax(35, MotorType.kBrushed);
        public static final Supplier<FridolinsMotor> frontLeftMotorInitializer = () -> new FridoCANSparkMax(33, MotorType.kBrushed);
        public static final Supplier<FridolinsMotor> backLeftMotorInitializer = () -> new FridoCANSparkMax(37, MotorType.kBrushed);

        // public static final Supplier<FridolinsMotor> frontRightMotorInitializer = ()
        // -> new FridoCANSparkMax(12, MotorType.kBrushless);
        // public static final Supplier<FridolinsMotor> backRightMotorInitializer = ()
        // -> new FridoCANSparkMax(13, MotorType.kBrushless);
        // public static final Supplier<FridolinsMotor> frontLeftMotorInitializer = ()
        // -> new FridoCANSparkMax(10, MotorType.kBrushless);
        // public static final Supplier<FridolinsMotor> backLeftMotorInitializer = () ->
        // new FridoCANSparkMax(11, MotorType.kBrushless);

        public static final int ticksPerRotation = 512;
        public static final double wheelDiameter = 0.10;
        public static final Supplier<Translation2d> frontLeftWheelDisplacementMeters = () -> new Translation2d(0.26,
                0.19);
        public static final Supplier<Translation2d> frontRightWheelDisplacementMeters = () -> new Translation2d(-0.26,
                0.19);
        public static final Supplier<Translation2d> backLeftWheelDisplacementMeters = () -> new Translation2d(0.26,
                -0.19);
        public static final Supplier<Translation2d> backRightWheelDisplacementMeters = () -> new Translation2d(-0.26,
                -0.19);

        public static final double defaultSpeedFac1or = 1.0;
		public static final boolean frontLeftMotorInverted = true;
		public static final boolean frontRightMotorInverted = false;
		public static final boolean backLeftMotorInverted = true;
        public static final boolean backRightMotorInverted = false;
        public static final boolean frontLeftEncoderInverted = true;
        public static final boolean frontRightEncoderInverted = false;
        public static final boolean backLeftEncoderInverted = true;
        public static final boolean backRightEncoderInverted = false;
    }
}