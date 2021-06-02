package frc.robot.commands.Thrower;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.commands.TimerCommand;
import frc.robot.commands.ballPickUp.LoadBallCommand;

public class FlowForwardCommandGroup extends SequentialCommandGroup{
    public FlowForwardCommandGroup() {
        Command shootCommand = new ShootCommand();
        addCommands(new AimTurretCommandGroup(), shootCommand, new TimerCommand(1), new LoadBallCommand(), new TimerCommand(1), new StopThrowerCommandGroup());
    }
}
