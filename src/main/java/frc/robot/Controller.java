package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Constants;
import frc.robot.commands.ZeroNavx;
import frc.robot.commands.ballPickUp.BallPickUpCommand;
import frc.robot.commands.ballPickUp.LoadBallCommand;
import frc.robot.commands.ballPickUp.ReleaseBallCommand;
import frc.robot.commands.swerve.BreakCommand;
import frc.robot.commands.swerve.FieldOriented;
import frc.robot.commands.swerve.PickupOriented;
import frc.robot.commands.swerve.SetSpeedFactor;
import frc.robot.commands.swerve.ThrowerOriented;
import frc.robot.commands.swerve.ZeroEncoders;

public class Controller {
    private static Controller instance;

    public DriveJoystick driveJoystick;
    public ControlJoystick controlJoystick;

    private Controller() {
        driveJoystick = new DriveJoystick();
        controlJoystick = new ControlJoystick();
    }

    public static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    // Class for basic Joystick functionality
    public class SuperJoystick {
        Joystick controller;

        public SuperJoystick(int joystickId) {
            controller = new Joystick(joystickId);
        }

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

    private Runnable runCommandAndCancelWhenPressedAgain(CommandBase command) {
        return () -> {
            if (CommandScheduler.getInstance().isScheduled(command))
                CommandScheduler.getInstance().cancel(command);
            else
                CommandScheduler.getInstance().schedule(command);
        };
    }

    public class DriveJoystick extends SuperJoystick {
        // Define Buttons to make Bindings
        JoystickButton zeroEncodersButton;
        JoystickButton fieldOrientedButton;
        JoystickButton throwerOrientedButton;
        JoystickButton pickupOrientedButton;
        JoystickButton slowSpeedFactorButton;
        JoystickButton zeroNavxButton;
        JoystickButton breakButton;

        public DriveJoystick() {
            super(Constants.Joystick.DRIVER_ID);
            configureButtonBindings();
        }

        public void configureButtonBindings() {
            // Initialize the buttons
            zeroEncodersButton = new JoystickButton(controller, Constants.SwerveDrive.ButtounIds.zeroEncoders);
            fieldOrientedButton = new JoystickButton(controller, Constants.SwerveDrive.ButtounIds.fieledOriented);
            throwerOrientedButton = new JoystickButton(controller, Constants.SwerveDrive.ButtounIds.throwerOriented);
            pickupOrientedButton = new JoystickButton(controller, Constants.SwerveDrive.ButtounIds.pickupOriented);
            slowSpeedFactorButton = new JoystickButton(controller, Constants.SwerveDrive.ButtounIds.slowSpeedMode);
            zeroNavxButton = new JoystickButton(controller, Constants.zeroNavxButtonID);
            breakButton = new JoystickButton(controller, Constants.SwerveDrive.ButtounIds.breakButton);

            // Configure the binding
            zeroEncodersButton.whenPressed(runCommandAndCancelWhenPressedAgain(new ZeroEncoders()));
            fieldOrientedButton.whenPressed(new FieldOriented());
            throwerOrientedButton.whenPressed(new ThrowerOriented());
            pickupOrientedButton.whenPressed(new PickupOriented());
            slowSpeedFactorButton.whenPressed(new SetSpeedFactor(Constants.SwerveDrive.slowSpeedFactor));
            slowSpeedFactorButton.whenReleased(new SetSpeedFactor(Constants.SwerveDrive.defaultSpeedFactor));
            zeroNavxButton.whenPressed(new ZeroNavx());
            breakButton.whileHeld(new BreakCommand());
        }
    }

    public class ControlJoystick extends SuperJoystick {
        // Define Buttons to make Bindings
        JoystickButton pickUpButton; // RbButton
        JoystickButton releaseButton; // LtButton
        JoystickButton loadButton;

        public ControlJoystick() {
            super(Constants.Joystick.CONTROL_ID);
            configureButtonBindings();
        }

        public void configureButtonBindings() {
            // Initialize the buttons
            pickUpButton = new JoystickButton(controller, Constants.Joystick.RB_BUTTON_ID);
            releaseButton = new JoystickButton(controller, Constants.Joystick.LT_BUTTON_ID);
            loadButton = new JoystickButton(controller, Constants.Joystick.LB_BUTTON_ID);

            // Configure the bindings
            pickUpButton.whenPressed(runCommandAndCancelWhenPressedAgain(new BallPickUpCommand()));
            releaseButton.whenPressed(new ReleaseBallCommand());
            loadButton.whenPressed(new LoadBallCommand());
        }
    }
}