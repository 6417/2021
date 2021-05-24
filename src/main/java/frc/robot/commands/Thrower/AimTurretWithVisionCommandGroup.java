package frc.robot.commands.Thrower;

import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

public class AimTurretWithVisionCommandGroup extends ParallelCommandGroup{
   public AimTurretWithVisionCommandGroup() {
        addCommands(new SetTurretShootingDirectionCommand(), new SetTurretShootingAngleCommand());
   } 
}
