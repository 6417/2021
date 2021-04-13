/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import java.util.function.Supplier;

import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import frc.robot.utilities.fridolinsMotor.FridoCANSparkMax;
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
        public static final boolean IS_ENABLED = false;
    }

    public static class BallPickUp{
        public static boolean isEnabled = true;

        // MotorIDs
        public static final int pickUpMotor_ID = 12;   
        public static final int tunnelMotor_ID = 10;   

        // MotorSuppliers
        public static Supplier <FridolinsMotor> pickUpMotor = () -> new FridoCANSparkMax(pickUpMotor_ID, MotorType.kBrushless);
        public static Supplier <FridolinsMotor> tunnelMotor = () -> new FridoCANSparkMax(tunnelMotor_ID, MotorType.kBrushless);

        
        // variables to find out the ballcolor
        public static final int comparativeValueBlueLow = 52;
        public static final int comparativeValueBlueHigh = 70;
        public static final int comparativeValueRedLow = 100;
        public static final int comparativeValueRedTwo = 65;

        public static final double ticksForTunnelMotor = 0; 

        public static final int countsPerRevTunnelMotor = 1; 
        public static final int countsPerRevPickUpMotor = 1; 

        public static final double pickUpSpeed = 0.7; // TODO optimial speed

        public static final boolean isLightBarrierInverted = false;
    }
}
