package frc.robot.commands.Thrower;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.ThrowerSubsystem;

public class StopThrowerCommandGroup extends CommandBase{
    public StopThrowerCommandGroup() {

    }

    @Override
    public void initialize() {
        ThrowerSubsystem.getInstance().runShooter(0);
        ThrowerSubsystem.getInstance().runLoaderMotor(0);
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
