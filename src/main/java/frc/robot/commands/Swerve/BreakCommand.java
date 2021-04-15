package frc.robot.commands.swerve;

import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.subsystems.swerve.SwerveDrive;

public class BreakCommand extends CommandBase {
    public BreakCommand() {
        addRequirements(SwerveDrive.getInstance());
    }

    @Override
    public void execute() {
        SwerveDrive.getInstance().forEachModule((module) -> module.setDriveMotorSpeed(0.0));
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}