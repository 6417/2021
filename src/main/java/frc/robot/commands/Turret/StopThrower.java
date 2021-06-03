package frc.robot.commands.Turret;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.TurretSubsystem;

public class StopThrower extends CommandBase{
    public StopThrower() {

    }

    @Override
    public void initialize() {
        TurretSubsystem.getInstance().runShooter(0);
        TurretSubsystem.getInstance().runLoaderMotor(0);
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
