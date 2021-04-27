package frc.robot.utilities;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.Constants;
import frc.robot.Robot;
import frc.robot.commands.ballPickUp.BallPickUpCommand;
import frc.robot.commands.ballPickUp.LoadBallCommand;
import frc.robot.commands.ballPickUp.ReleaseBallCommand;
import frc.robot.commands.swerve.FieldOriented;
import frc.robot.commands.swerve.PickupOriented;
import frc.robot.commands.swerve.SetSpeedFactor;
import frc.robot.commands.swerve.ThrowerOriented;
import frc.robot.commands.swerve.ZeroEncoders;
import frc.robot.commands.Thrower.CalibrateShootingAngleCommand;
import frc.robot.commands.Thrower.CalibrateTurretShootingDirectionCommand;
import frc.robot.commands.Thrower.SetTurretShootingAngleCommand;
import frc.robot.commands.Thrower.SetTurretShootingDirectionCommand;
import frc.robot.commands.Thrower.ShootCommand;
import frc.robot.subsystems.ThrowerSubsystem;

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
        JoystickButton calibrateThrowerButton;

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
            zeroNavxButton.whenPressed(Robot.getNavx()::reset);
            // Configure the bindings
            calibrateThrowerButton.whenPressed(new CalibrateShootingAngleCommand());
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

        JoystickButton calibrateThrowerButton;
        JoystickButton runAngleMotorButton;
        JoystickButton setAngleMotorPositionButton;
        JoystickButton shootButton;

        public ControlJoystick() {
            super(Constants.Joystick.CONTROL_ID);
            configureButtonBindings();
        }

        public void configureButtonBindings() {
            // Initialize the buttons
            calibrateThrowerButton = new JoystickButton(controller, Constants.Joystick.A_BUTTON_ID);
            runAngleMotorButton = new JoystickButton(controller, Constants.Joystick.X_BUTTON_ID);
            setAngleMotorPositionButton = new JoystickButton(controller, Constants.Joystick.Y_BUTTON_ID);
            shootButton = new JoystickButton(controller, Constants.Joystick.B_BUTTON_ID);

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

            calibrateThrowerButton.whenPressed(new CalibrateShootingAngleCommand());
            runAngleMotorButton.whileHeld(() -> ThrowerSubsystem.getInstance().runShootingAngleMotor(controller.getY() * 0.2));
            runAngleMotorButton.whenReleased(() -> ThrowerSubsystem.getInstance().runShootingAngleMotor(0));
            setAngleMotorPositionButton.whenPressed(new SetTurretShootingAngleCommand());
            shootButton.whileHeld(new ShootCommand());
        }
    }
}