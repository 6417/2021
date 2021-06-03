package frc.robot;

import java.util.Optional;

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.commands.ballPickUp.BallPickUpCommand;
import frc.robot.commands.ballPickUp.LoadBallCommand;
import frc.robot.commands.ballPickUp.ReleaseBallCommand;
import frc.robot.subsystems.mecanum.MecanumDriveSubsystem;
import frc.robot.subsystems.swerve.SwerveDrive;

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

    public static Runnable runCommandAndCancelWhenPressedAgain(CommandBase command) {
        return () -> {
            if (CommandScheduler.getInstance().isScheduled(command))
                CommandScheduler.getInstance().cancel(command);
            else
                CommandScheduler.getInstance().schedule(command);
        };
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


    public class DriveJoystick extends SuperJoystick {
        private DriveJoystick() {
            super(Constants.Joystick.DRIVER_ID);
            controller.ifPresent(this::configureButtonBindings);
        }

        private void configureButtonBindings(Joystick joystick) {
            SwerveDrive.getInstance().configureButtonBindings(joystick);    
            MecanumDriveSubsystem.getInstance().configureButtonBindings(joystick);
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
            releaseButton.whenPressed(runCommandAndCancelWhenPressedAgain(new ReleaseBallCommand()));
            loadButton.whenPressed(new LoadBallCommand());
        }
    }
}