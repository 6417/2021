package frc.robot.commands.Thrower;

import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import frc.robot.subsystems.ThrowerSubsystem;

public class AimTurretWithVisionCommandGroup extends ParallelCommandGroup{
   public AimTurretWithVisionCommandGroup() {
        addCommands(new SetTurretShootingDirectionCommand(), new SetTurretShootingAngleCommand());
        addRequirements(ThrowerSubsystem.getInstance());
   } 
}
