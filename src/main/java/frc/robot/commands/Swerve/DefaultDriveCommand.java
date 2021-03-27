package frc.robot.commands.Swerve;

import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.subsystems.Swerve.SwerveDrive;
import frc.robot.utilities.Controller;
import frc.robot.utilities.ShuffleBoardInformation;
import frc.robot.utilities.Vector2d;

public class DefaultDriveCommand extends CommandBase {
    public DefaultDriveCommand() {
        addRequirements(SwerveDrive.getInstance());
    }

    private boolean joystickNotInDeadBand() {
        boolean result = false;
        result |= Constants.SwerveDrive.deadBand < Math.abs(Controller.getInstance().driveJoystick.getLeftStickX());
        result |= Constants.SwerveDrive.deadBand < Math.abs(Controller.getInstance().driveJoystick.getLeftStickY());
        result |= Constants.SwerveDrive.deadBand < Math.abs(Controller.getInstance().driveJoystick.getRightStickX());
        return result;
    }

    private double map(double x, double in_min, double in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    /**
     * @return index 0 is the maped x and index 1 is the maped y index 2 is the
     *         maped rotation
     */
    private double[] applyDeadBandToXYJoystick() {
        double[] output = new double[3];
        if (Math.abs(Controller.getInstance().driveJoystick.getLeftStickX()) > 0.0
                || Math.abs(Controller.getInstance().driveJoystick.getLeftStickY()) > 0.0) {
            Vector2d xyVector = new Vector2d(Controller.getInstance().driveJoystick.getLeftStickX(),
                    Controller.getInstance().driveJoystick.getLeftStickY());
            xyVector = xyVector.normalize();
            xyVector = xyVector.mult(map(
                    Math.hypot(Controller.getInstance().driveJoystick.getLeftStickX(),
                            Controller.getInstance().driveJoystick.getLeftStickY()),
                    Constants.SwerveDrive.deadBand, 1.0, 0.0, 1.0));
            output[0] = xyVector.x;
            output[1] = xyVector.y;
        } else {
            output[0] = 0.0;
            output[1] = 0.0;
        }
        if (Math.abs(Controller.getInstance().driveJoystick.getRightStickX()) > Constants.SwerveDrive.deadBand)
            output[2] = map(Controller.getInstance().driveJoystick.getRightStickX(), Constants.SwerveDrive.deadBand,
                    1.0, 0.0, 1.0);
        else
            output[2] = 0.0;
        return output;
    }

    @Override
    public void execute() {
        if (SwerveDrive.getInstance().areAllModulesZeroed() && joystickNotInDeadBand()) {
            double[] xyr = applyDeadBandToXYJoystick();
            double xSpeed = SwerveDrive.joystickInputToMetersPerSecond(xyr[0]);
            double ySpeed = SwerveDrive.joystickInputToMetersPerSecond(xyr[1]);
            if (Constants.SwerveDrive.joystickYinverted)
                ySpeed *= -1;
            if (Constants.SwerveDrive.joystickXinverted)
                xSpeed *= -1;
            double rotationSpeed = xyr[2] * Constants.SwerveDrive.maxRotationSpeed;
            SwerveDrive.getInstance().drive(new ChassisSpeeds(xSpeed, ySpeed, rotationSpeed));
        } else
            SwerveDrive.getInstance().forEachModule((module) -> module.stopDriveMotor());
    }
}