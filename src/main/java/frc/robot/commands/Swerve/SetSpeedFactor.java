package frc.robot.commands.swerve;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.swerve.SwerveDrive;

public class SetSpeedFactor extends CommandBase {
    private double speedFactor;
    public SetSpeedFactor(double speedFactor) {
        this.speedFactor = speedFactor;
    }

    @Override
    public void initialize() {
        SwerveDrive.getInstance().setSpeedFactor(speedFactor);
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}