package frc.robot.commands.Swerve;

import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.subsystems.Swerve.SwerveDrive;
import frc.robot.utilities.Controller;
import frc.robot.utilities.ShuffleBoardInformation;

public class DefaultDriveCommand extends CommandBase {
    public DefaultDriveCommand() {
        addRequirements(SwerveDrive.getInstance());
    }

    @Override
    public void execute() {
        if (SwerveDrive.getInstance().areAllModulesZeroed()) {
            double xSpeed = SwerveDrive
                    .joystickInputToMetersPerSecond(Controller.getInstance().driveJoystick.getLeftStickX());
            double ySpeed = SwerveDrive
                    .joystickInputToMetersPerSecond(Controller.getInstance().driveJoystick.getLeftStickY());
            if (Constants.SwerveDrive.joystickYinverted)
                ySpeed *= -1;
            if (Constants.SwerveDrive.joystickXinverted)
                xSpeed *= -1;
            double rotationSpeed = Controller.getInstance().driveJoystick.getRightStickX()
                    * Constants.SwerveDrive.maxRotationSpeed;
            SwerveDrive.getInstance().drive(new ChassisSpeeds(xSpeed, ySpeed, rotationSpeed));
        }
    }
}