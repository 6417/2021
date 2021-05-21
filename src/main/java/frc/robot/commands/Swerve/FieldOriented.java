package frc.robot.commands.swerve;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.Drive.DriveOrientation;
import frc.robot.subsystems.swerve.SwerveDrive;

public class FieldOriented extends CommandBase{
    @Override
    public void initialize() {
        SwerveDrive.getInstance().setDriveMode(DriveOrientation.FieldOriented);
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}