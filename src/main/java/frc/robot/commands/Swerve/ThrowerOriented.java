package frc.robot.commands.swerve;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.Drive.DriveMode;
import frc.robot.subsystems.swerve.SwerveDrive;

public class ThrowerOriented extends CommandBase{
    @Override
    public void initialize() {
        SwerveDrive.getInstance().setDriveMode(DriveMode.ThrowerOriented);
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}