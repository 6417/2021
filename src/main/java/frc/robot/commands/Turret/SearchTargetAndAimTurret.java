package frc.robot.commands.Turret;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.commands.TimerCommand;
import frc.robot.subsystems.TurretSubsystem;

public class SearchTargetAndAimTurret extends SequentialCommandGroup{
   public SearchTargetAndAimTurret() {
       addCommands(new FindTarget(), new AimTurret(), new TimerCommand(0.3), new AimTurret(), new TimerCommand(0.3), new AimTurret());
       addRequirements(TurretSubsystem.getInstance());
   } 
}
