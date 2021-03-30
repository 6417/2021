package frc.robot.utilities;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.Constants;
import frc.robot.commands.ZeroNavx;
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
        JoystickButton setAngleButton;
        JoystickButton shootButton;
        JoystickButton calibrateThrowerAngleButton;
        JoystickButton angleUpButton;

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
            calibrateThrowerButton = new JoystickButton(this.controller, Constants.Joystick.A_BUTTON_ID);
            setAngleButton = new JoystickButton(this.controller, Constants.Joystick.B_BUTTON_ID);
            shootButton = new JoystickButton(this.controller, Constants.Joystick.RT_BUTTON_ID);
            calibrateThrowerAngleButton = new JoystickButton(this.controller, Constants.Joystick.X_BUTTON_ID);
            angleUpButton = new JoystickButton(this.controller, Constants.Joystick.LT_BUTTON_ID);

            // Configure the binding
            zeroEncodersButton.whenPressed(new ZeroEncoders());
            fieldOrientedButton.whenPressed(new FieldOriented());
            throwerOrientedButton.whenPressed(new ThrowerOriented());
            pickupOrientedButton.whenPressed(new PickupOriented());
            slowSpeedFactorButton.whenPressed(new SetSpeedFactor(Constants.SwerveDrive.slowSpeedFactor));
            slowSpeedFactorButton.whenReleased(new SetSpeedFactor(Constants.SwerveDrive.defaultSpeedFactor));
            zeroNavxButton.whenPressed(new ZeroNavx());
            // Configure the bindings
            calibrateThrowerButton.whenPressed(new CalibrateTurretShootingDirectionCommand());
            shootButton.whileHeld(new ShootCommand());
            calibrateThrowerAngleButton.whenPressed(new CalibrateShootingAngleCommand());
            angleUpButton.whileHeld(() -> ThrowerSubsystem.getInstance().runShootingAngleMotor(0.2));
            angleUpButton.whenReleased(() -> ThrowerSubsystem.getInstance().runShootingAngleMotor(0));
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