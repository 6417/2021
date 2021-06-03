package frc.robot.commands.Turret;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.commands.TimerCommand;
import frc.robot.commands.ballPickUp.LoadBallCommand;

public class ShootAndLoad extends SequentialCommandGroup{
    public ShootAndLoad() {
        Command shootCommand = new RunShooter();
        addCommands(new SearchTargetAndAimTurret(), shootCommand, new TimerCommand(1), new LoadBallCommand(), new TimerCommand(1), new StopThrower());
    }
}
