package frc.robot.commands.mecanum;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.MecanumDriveSubsystem;
import frc.robot.subsystems.Drive.DriveMode;

public class FieldOriented extends CommandBase{
    @Override
    public void initialize() {
        MecanumDriveSubsystem.getInstance().setDriveMode(DriveMode.FieldOriented);
    }
}