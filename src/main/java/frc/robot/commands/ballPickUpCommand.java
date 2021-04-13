package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.PickUpSubsystem;

public class BallPickUpCommand extends CommandBase {

    public BallPickUpCommand() {
       addRequirements(PickUpSubsystem.getInstance()); 
    }

    @Override
    public void initialize(){
    }

    @Override
    public void execute(){
        PickUpSubsystem.getInstance().pickUpBall();
    }

    @Override
    public void end(boolean interupted){
        PickUpSubsystem.getInstance().stopMotors();
    }

    @Override
    public boolean isFinished(){
        return false;
    }
}