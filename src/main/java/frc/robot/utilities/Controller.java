package frc.robot.utilities;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.Constants;
import frc.robot.commands.BallPickUpCommand;
import frc.robot.commands.LoadBallCommand;
import frc.robot.commands.ReleaseBallCommand;

public class Controller {

    private static Controller mInstance;

    public DriveJoystick driveJoystick;
    public ControlJoystick controlJoystick;

    public Controller() {
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

        public DriveJoystick() {
            super();
            super.controller = new Joystick(Constants.Joystick.DRIVER_ID);
            configureButtonBindings();
        }

        public void configureButtonBindings() {
            // Initialize the buttons

            // Configure the bindings
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
            super();
            super.controller = new Joystick(Constants.Joystick.CONTROL_ID);
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