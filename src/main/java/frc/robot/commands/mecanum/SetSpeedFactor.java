package frc.robot.commands.mecanum;

import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.subsystems.mecanum.MecanumDriveSubsystem;

public class SetSpeedFactor extends CommandBase {
    private double speedFactor;

    public SetSpeedFactor(double speedFactor) {
        this.speedFactor = speedFactor;
    }

    @Override
    public void initialize() {
        MecanumDriveSubsystem.getInstance().setSpeedFactor(speedFactor);
    }

    @Override
    public void end(boolean interrupted) {
        MecanumDriveSubsystem.getInstance().setSpeedFactor(Constants.MecanumDrive.defaultSpeedFac1or);
    }
}