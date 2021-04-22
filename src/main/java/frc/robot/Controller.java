package frc.robot;

import static frc.robot.Robot.getNavx;

import java.util.Optional;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Constants;
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

    public SuperJoystick driveJoystick;
    public SuperJoystick controlJoystick;

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
        Optional<Joystick> controller;

        public SuperJoystick(int joystickId) {
            if (new Joystick(joystickId).isConnected())
                controller = Optional.of(new Joystick(joystickId));
            else
                controller = Optional.empty();
        }

        public double getLeftStickY() {
            return controller.map((joystick) -> joystick.getY()).orElse(0.0);
        }

        public double getLeftStickX() {
            return controller.map((joystick) -> joystick.getX()).orElse(0.0);
        }

        public double getRightStickY() {
            return controller.map((joystick) -> joystick.getThrottle()).orElse(0.0);
        }

        public double getRightStickX() {
            return controller.map((joystick) -> joystick.getTwist()).orElse(0.0);
        }

        public double getDpad() {
            return controller.map((joystick) -> joystick.getPOV()).orElse(0);
        }

        public void setRumble(RumbleType type, double value) {
            controller.ifPresent((Joystick joystick) -> joystick.setRumble(type, value));
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
        JoystickButton fullSpeedButton;

        private DriveJoystick() {
            super(Constants.Joystick.DRIVER_ID);
            controller.ifPresent((ignored) -> this.configureButtonBindings());
        }

        private void configureButtonBindings() {
            // Initialize the buttons
            zeroEncodersButton = new JoystickButton(controller.get(), Constants.SwerveDrive.ButtounIds.zeroEncoders);
            fieldOrientedButton = new JoystickButton(controller.get(), Constants.SwerveDrive.ButtounIds.fieledOriented);
            throwerOrientedButton = new JoystickButton(controller.get(),
                    Constants.SwerveDrive.ButtounIds.throwerOriented);
            pickupOrientedButton = new JoystickButton(controller.get(),
                    Constants.SwerveDrive.ButtounIds.pickupOriented);
            slowSpeedFactorButton = new JoystickButton(controller.get(),
                    Constants.SwerveDrive.ButtounIds.slowSpeedMode);
            zeroNavxButton = new JoystickButton(controller.get(), Constants.zeroNavxButtonID);
            breakButton = new JoystickButton(controller.get(), Constants.SwerveDrive.ButtounIds.breakButton);
            fullSpeedButton = new JoystickButton(controller.get(), Constants.SwerveDrive.ButtounIds.fullSpeed);

            // Configure the binding
            zeroEncodersButton.whenPressed(runCommandAndCancelWhenPressedAgain(new ZeroEncoders()));
            fieldOrientedButton.whenPressed(new FieldOriented());
            throwerOrientedButton.whenPressed(new ThrowerOriented());
            pickupOrientedButton.whenPressed(new PickupOriented());
            CommandBase slowSpeedCommand = new SetSpeedFactor(Constants.SwerveDrive.slowSpeedFactor);
            slowSpeedFactorButton.whenPressed(() -> {
                if (!CommandScheduler.getInstance().isScheduled(slowSpeedCommand))
                    CommandScheduler.getInstance().schedule(slowSpeedCommand);
                else
                    CommandScheduler.getInstance()
                            .schedule(new SetSpeedFactor(Constants.SwerveDrive.defaultSpeedFactor));
            });
            zeroNavxButton.whenPressed(getNavx()::reset);
            breakButton.whileHeld(new BreakCommand());
            CommandBase fullSpeedCommand = new SetSpeedFactor(1.0);
            fullSpeedButton.whenPressed(() -> {
                if (!CommandScheduler.getInstance().isScheduled(fullSpeedCommand))
                    CommandScheduler.getInstance().schedule(fullSpeedCommand);
                else
                    CommandScheduler.getInstance()
                            .schedule(new SetSpeedFactor(Constants.SwerveDrive.defaultSpeedFactor));
            });
        }
    }

    public class ControlJoystick extends SuperJoystick {
        // Define Buttons to make Bindings
        JoystickButton pickUpButton; // RbButton
        JoystickButton releaseButton; // LtButton
        JoystickButton loadButton;

        private ControlJoystick() {
            super(Constants.Joystick.CONTROL_ID);
            controller.ifPresent((ignored) -> this.configureButtonBindings());
        }

        private void configureButtonBindings() {
            // Initialize the buttons
            pickUpButton = new JoystickButton(controller.get(), Constants.Joystick.RB_BUTTON_ID);
            releaseButton = new JoystickButton(controller.get(), Constants.Joystick.LT_BUTTON_ID);
            loadButton = new JoystickButton(controller.get(), Constants.Joystick.LB_BUTTON_ID);

            // Configure the bindings
            pickUpButton.whenPressed(runCommandAndCancelWhenPressedAgain(new BallPickUpCommand()));
            releaseButton.whenPressed(new ReleaseBallCommand());
            loadButton.whenPressed(new LoadBallCommand());
        }
    }
}