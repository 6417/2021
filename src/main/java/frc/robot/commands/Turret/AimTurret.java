package frc.robot.commands.Turret;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class AimTurret extends SequentialCommandGroup{
   public AimTurret() {
        addCommands(new AimTurretDirection(), new AimTurretAngle());
   } 
}
