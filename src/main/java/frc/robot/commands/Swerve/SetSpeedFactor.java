package frc.robot.commands.swerve;

import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.subsystems.swerve.SwerveDrive;

public class SetSpeedFactor extends CommandBase {
    private double speedFactor;
    private static SubsystemBase speedFactorCommandRequirement = new SubsystemBase() {};
    public SetSpeedFactor(double speedFactor) {
        this.speedFactor = speedFactor;
        addRequirements(speedFactorCommandRequirement);
    }

    @Override
    public void initialize() {
        SwerveDrive.getInstance().setSpeedFactor(speedFactor);
    }

    @Override
    public void end(boolean interrupted) {
        SwerveDrive.getInstance().setSpeedFactor(Constants.SwerveDrive.defaultSpeedFactor);
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}