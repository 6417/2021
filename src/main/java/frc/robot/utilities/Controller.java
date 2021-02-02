package frc.robot.utilities;

import edu.wpi.first.wpilibj.Joystick;
import frc.robot.Constants;

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

        public double getPOV() {
            return controller.getPOV();
        }

        public boolean getButton() {
            return controller.getRawButton(Constants.Joystick.X_BUTTON_ID);
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