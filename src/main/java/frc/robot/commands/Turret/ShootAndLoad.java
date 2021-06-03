package frc.robot.commands.Turret;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.commands.TimerCommand;
import frc.robot.commands.ballPickUp.BallPickUpCommand;
import frc.robot.commands.ballPickUp.LoadBallCommand;
import frc.robot.subsystems.PickUpSubsystem;

public class ShootAndLoad extends SequentialCommandGroup{
    public ShootAndLoad() {
        Command shootCommand = new RunShooter();
        addCommands(new SearchTargetAndAimTurret(), shootCommand, new TimerCommand(1), new LoadBallCommand(), new TimerCommand(1), new StopThrower());
    }

    @Override
    public void end(boolean interrupted) {
        super.end(interrupted);
        PickUpSubsystem.getInstance().resetBallColor();
    }
}
