package frc.robot.commands.swerve;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.swerve.SwerveDrive;

public class CentricSwerveMode extends CommandBase{
    @Override
    public void initialize() {
        SwerveDrive.getInstance().setCentricSwerveMode(true);
    }

    @Override
    public void end(boolean interrupted) {
        SwerveDrive.getInstance().setCentricSwerveMode(false);
    }
}