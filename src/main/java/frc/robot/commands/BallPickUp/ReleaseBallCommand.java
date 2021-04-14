package frc.robot.commands.BallPickUp;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.PickUpSubsystem;

public class ReleaseBallCommand extends CommandBase{
    
    public ReleaseBallCommand(){
        addRequirements(PickUpSubsystem.getInstance());
    }

    @Override
    public void initialize(){
    }

    @Override
    public void execute(){
        PickUpSubsystem.getInstance().releaseBall();
    }

    @Override
    public void end(boolean interupted){
    }

    @Override
    public boolean isFinished(){
        return false;
    }
}