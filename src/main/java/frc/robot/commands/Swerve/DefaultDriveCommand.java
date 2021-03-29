package frc.robot.commands.Swerve;

import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.subsystems.Swerve.SwerveDrive;
import frc.robot.utilities.Controller;
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

    private static class JoystickInput {
        public double x;
        public double y;
        public double r;
    }

    private Vector2d getXYvectorWithAppliedDeadBandFromJoystick() {
        Vector2d xyVector = new Vector2d(Controller.getInstance().driveJoystick.getLeftStickX(),
                Controller.getInstance().driveJoystick.getLeftStickY());
        xyVector = xyVector.normalize();
        double xyVectorLength = Math.hypot(Controller.getInstance().driveJoystick.getLeftStickX(),
                Controller.getInstance().driveJoystick.getLeftStickY());
        xyVector = xyVector.mult(map(xyVectorLength, Constants.SwerveDrive.deadBand, 1.0, 0.0, 1.0));
        return xyVector;
    }

    private JoystickInput applyDeadBandToXandY() {
        JoystickInput result = new JoystickInput();
        boolean xyNotInDeadBand = Math.abs(Controller.getInstance().driveJoystick.getLeftStickX()) > 0.0
                || Math.abs(Controller.getInstance().driveJoystick.getLeftStickY()) > 0.0;
        if (xyNotInDeadBand) {
            Vector2d xyVector = getXYvectorWithAppliedDeadBandFromJoystick();
            result.x = xyVector.x;
            result.y = xyVector.y;
        } else {
            result.x = 0.0;
            result.y = 0.0;
        }
        return result;
    }

    private double getJoystickRotationWithAppliedDeadBand() {
        boolean rotaionNotInDeadBand = Math
                .abs(Controller.getInstance().driveJoystick.getRightStickX()) > Constants.SwerveDrive.deadBand;
        if (rotaionNotInDeadBand)
            return map(Controller.getInstance().driveJoystick.getRightStickX(), Constants.SwerveDrive.deadBand, 1.0,
                    0.0, 1.0);
        return 0.0;
    }

    private JoystickInput applyDeadBandToJoystickInput() {
        JoystickInput result = applyDeadBandToXandY();
        result.r = getJoystickRotationWithAppliedDeadBand();
        return result;
    }

    private Vector2d convertJoystickInputToVector(JoystickInput xyr) {
        double xSpeed = SwerveDrive.joystickInputToMetersPerSecond(xyr.x);
        double ySpeed = SwerveDrive.joystickInputToMetersPerSecond(xyr.y);
        if (Constants.SwerveDrive.joystickYinverted)
            ySpeed *= -1;
        if (Constants.SwerveDrive.joystickXinverted)
            xSpeed *= -1;
        return new Vector2d(xSpeed, ySpeed);
    }

    @Override
    public void execute() {
        if (SwerveDrive.getInstance().areAllModulesZeroed() && joystickNotInDeadBand()) {
            JoystickInput xyr = applyDeadBandToJoystickInput();
            Vector2d xyVector = convertJoystickInputToVector(xyr);
            double rotationSpeed = xyr.r * Constants.SwerveDrive.maxRotationSpeed;
            SwerveDrive.getInstance().drive(new ChassisSpeeds(xyVector.x, xyVector.y, rotationSpeed));
        } else
            SwerveDrive.getInstance().stopAllMotors();
    }
}