package frc.robot.utilities;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.Constants;
import frc.robot.commands.Swerve.FieldOriented;
import frc.robot.commands.Swerve.PickupOriented;
import frc.robot.commands.Swerve.SetSpeedFactor;
import frc.robot.commands.Swerve.ThrowerOriented;
import frc.robot.commands.Swerve.ZeroEncoders;

public class Controller {

    private static Controller mInstance;

    public DriveJoystick driveJoystick;
    public ControlJoystick controlJoystick;

    private Controller() {
        driveJoystick = new DriveJoystick();
        controlJoystick = new ControlJoystick();
    }

    public static Controller getInstance() {
        if (mInstance == null) {
            mInstance = new Controller();
        }
        return mInstance;
    }

    // Class for basic Joystick functionality
    public class SuperJoystick {
        Joystick controller = new Joystick(-1);

        public double getLeftStickY() {
            return controller.getY();
        }

        public double getLeftStickX() {
            return controller.getX();
        }

        public double getRightStickY() {
            return controller.getThrottle();
        }

        public double getRightStickX() {
            return controller.getTwist();
        }

        public double getDpad() {
            return controller.getPOV();
        }
    }

    public class DriveJoystick extends SuperJoystick {
        // Define Buttons to make Bindings
        JoystickButton zeroEncodersButton;
        JoystickButton fieldOrientedButton;
        JoystickButton throwerOrientedButton;
        JoystickButton pickupOrientedButton;
        JoystickButton slowSpeedFactorButton;

        public DriveJoystick() {
            super();
            super.controller = new Joystick(Constants.Joystick.DRIVER_ID);
            configureButtonBindings();
        }

        public void configureButtonBindings() {
            // Initialize the buttons
            zeroEncodersButton = new JoystickButton(controller, Constants.SwerveDrive.ButtounIds.zeroEncoders);
            fieldOrientedButton = new JoystickButton(controller, Constants.SwerveDrive.ButtounIds.fieledOriented);
            throwerOrientedButton = new JoystickButton(controller, Constants.SwerveDrive.ButtounIds.throwerOriented);
            pickupOrientedButton = new JoystickButton(controller, Constants.SwerveDrive.ButtounIds.pickupOriented);
            slowSpeedFactorButton = new JoystickButton(controller, Constants.SwerveDrive.ButtounIds.slowSpeedMode);

            // Configure the binding
            zeroEncodersButton.whenPressed(new ZeroEncoders());
            fieldOrientedButton.whenPressed(new FieldOriented());
            throwerOrientedButton.whenPressed(new ThrowerOriented());
            pickupOrientedButton.whenPressed(new PickupOriented());
            slowSpeedFactorButton.whenPressed(new SetSpeedFactor(Constants.SwerveDrive.slowSpeedFactor));
            slowSpeedFactorButton.whenReleased(new SetSpeedFactor(Constants.SwerveDrive.defaultSpeedFactor));
        }
    }

    public class ControlJoystick extends SuperJoystick {
        // Define Buttons to make Bindings

        public ControlJoystick() {
            super();
            super.controller = new Joystick(Constants.Joystick.CONTROL_ID);
            configureButtonBindings();
        }

        public void configureButtonBindings() {
            // Initialize the buttons

            // Configure the bindings

        }
    }
}