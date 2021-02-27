package frc.robot.commands.Swerve;

import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.Swerve.SwerveDrive;
import frc.robot.utilities.Controller;

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
            double rotationSpeed = SwerveDrive
                    .joystickInputToMetersPerSecond(Controller.getInstance().driveJoystick.getRightStickX());
            SwerveDrive.getInstance().drive(new ChassisSpeeds(xSpeed, ySpeed, rotationSpeed));
        }
    }
}