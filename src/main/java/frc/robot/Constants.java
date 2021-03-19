/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import java.util.function.Supplier;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.geometry.Translation2d;
import frc.robot.utilities.fridolinsMotor.FridoTalon;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants.  This class should not be used for any other purpose.  All constants should be
 * declared globally (i.e. public static).  Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {
    public static class Joystick{
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

    public static class Vision{
        public static final boolean IS_ENABLED = true;
    }
    public static class TankDrive{
        public static final boolean IS_ENABLED = true;

        public static final int MOTOR_DRIVE_FRONT_RIGHT_ID = 12;
        public static final int MOTOR_DRIVE_FRONT_LEFT_ID = 10;
        public static final int MOTOR_DRIVE_BACK_RIGHT_ID = 13;
        public static final int MOTOR_DRIVE_BACK_LEFT_ID = 11;
        public static final Supplier<FridolinsMotor> frontRightMotorInitializer = () -> new FridoTalon(MOTOR_DRIVE_FRONT_RIGHT_ID, 1, 0);
        public static final Supplier<FridolinsMotor> backRightMotorInitializer = () -> new FridoTalon(MOTOR_DRIVE_BACK_RIGHT_ID, 3, 2);
        public static final Supplier<FridolinsMotor> frontLeftMotorInitializer = () -> new FridoTalon(MOTOR_DRIVE_FRONT_LEFT_ID, 5, 4);
        public static final Supplier<FridolinsMotor> backLeftMotorInitializer = () -> new FridoTalon(MOTOR_DRIVE_BACK_LEFT_ID, 7, 6);

        // public static final Supplier<FridolinsMotor> frontRightMotorInitializer = () -> new FridoCANSparkMax(12, MotorType.kBrushless);
        // public static final Supplier<FridolinsMotor> backRightMotorInitializer = () -> new FridoCANSparkMax(13, MotorType.kBrushless);
        // public static final Supplier<FridolinsMotor> frontLeftMotorInitializer = () -> new FridoCANSparkMax(10, MotorType.kBrushless);
        // public static final Supplier<FridolinsMotor> backLeftMotorInitializer = () -> new FridoCANSparkMax(11, MotorType.kBrushless);

        public static final int ticksPerRotation = 512;
        public static final double wheelDiameter = 0.133;
        public static final Supplier<Translation2d> frontLeftWheelDisplacementMeters = () -> new Translation2d(0.26, 0.19);
        public static final Supplier<Translation2d> frontRightWheelDisplacementMeters = () -> new Translation2d(-0.26, 0.19);
        public static final Supplier<Translation2d> backLeftWheelDisplacementMeters  = () -> new Translation2d(0.26, -0.19);
        public static final Supplier<Translation2d> backRightWheelDisplacementMeters = () -> new Translation2d(-0.26, -0.19);

        public static final Supplier<AHRS> navxInitializer = () -> new AHRS();
    }
}
