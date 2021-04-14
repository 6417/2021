package frc.robot.utilities;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Constants;
import frc.robot.commands.ZeroNavx;
import frc.robot.commands.BallPickUp.BallPickUpCommand;
import frc.robot.commands.BallPickUp.LoadBallCommand;
import frc.robot.commands.BallPickUp.ReleaseBallCommand;
import frc.robot.commands.Swerve.FieldOriented;
import frc.robot.commands.Swerve.PickupOriented;
import frc.robot.commands.Swerve.SetSpeedFactor;
import frc.robot.commands.Swerve.ThrowerOriented;
import frc.robot.commands.Swerve.ZeroEncoders;

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

    public class DriveJoystick extends SuperJoystick {
        // Define Buttons to make Bindings
        JoystickButton zeroEncodersButton;
        JoystickButton fieldOrientedButton;
        JoystickButton throwerOrientedButton;
        JoystickButton pickupOrientedButton;
        JoystickButton slowSpeedFactorButton;
        JoystickButton zeroNavxButton;

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

            // Configure the binding
            zeroEncodersButton.whenPressed(new ZeroEncoders());
            fieldOrientedButton.whenPressed(new FieldOriented());
            throwerOrientedButton.whenPressed(new ThrowerOriented());
            pickupOrientedButton.whenPressed(new PickupOriented());
            slowSpeedFactorButton.whenPressed(new SetSpeedFactor(Constants.SwerveDrive.slowSpeedFactor));
            slowSpeedFactorButton.whenReleased(new SetSpeedFactor(Constants.SwerveDrive.defaultSpeedFactor));
            zeroNavxButton.whenPressed(new ZeroNavx());
        }
    }

    public class ControlJoystick extends SuperJoystick {
        // Define Buttons to make Bindings
        JoystickButton pickUpButton;    // RbButton
        BallPickUpCommand pickUpCommand;

        JoystickButton releaseButton;   //LtButton
        ReleaseBallCommand releaseBallCommand;

        JoystickButton loadButton;
        LoadBallCommand loadBallCommand;

        public ControlJoystick() {
            super(Constants.Joystick.CONTROL_ID);
            configureButtonBindings();
        }

        public void configureButtonBindings() {
            // Initialize the buttons

            // Configure the bindings
            pickUpButton = new JoystickButton(controller, Constants.Joystick.RB_BUTTON_ID);
            pickUpCommand = new BallPickUpCommand();
            pickUpButton.whenPressed(() -> {
                System.out.println("entered lambda");
                if (CommandScheduler.getInstance().isScheduled(pickUpCommand)){
                    CommandScheduler.getInstance().cancel(pickUpCommand);
                    System.out.println("entered if");
                }
                else{
                    CommandScheduler.getInstance().schedule(pickUpCommand);
                    System.out.println("entered else");
                }
            });

            releaseButton = new JoystickButton(controller, Constants.Joystick.LT_BUTTON_ID);
            releaseBallCommand = new ReleaseBallCommand();
            releaseButton.whenPressed(releaseBallCommand);

            loadButton = new JoystickButton(controller, Constants.Joystick.LB_BUTTON_ID);
            loadBallCommand = new LoadBallCommand();
            loadButton.whenPressed(loadBallCommand);
        }
    }
}