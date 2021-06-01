package frc.robot.commands.Thrower;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class AimTurretWithVisionCommandGroup extends SequentialCommandGroup{
   public AimTurretWithVisionCommandGroup() {
        addCommands(new SetTurretShootingDirectionCommand(), new SetTurretShootingAngleCommand());
   } 
}
