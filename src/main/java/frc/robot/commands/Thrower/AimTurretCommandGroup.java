package frc.robot.commands.Thrower;

import edu.wpi.first.wpilibj2.command.CommandGroupBase;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.commands.TimerCommand;
import frc.robot.subsystems.ThrowerSubsystem;

public class AimTurretCommandGroup extends SequentialCommandGroup{
   public AimTurretCommandGroup() {
       addCommands(new SetTurretShootingDirectionwithNavxCommand(), new AimTurretWithVisionCommandGroup(), new TimerCommand(0.3), new AimTurretWithVisionCommandGroup(), new TimerCommand(0.3), new AimTurretWithVisionCommandGroup());
       addRequirements(ThrowerSubsystem.getInstance());
   } 
}
