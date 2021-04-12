package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.PickUpSubsystem;
import frc.robot.subsystems.Base.PickUpBase;

public class ballPickUpCommand extends CommandBase{
    @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
    private final PickUpBase mPickUpSubsystem;

    public ballPickUpCommand(){
       mPickUpSubsystem = PickUpSubsystem.getInstance();
       addRequirements(mPickUpSubsystem); 
    }

    @Override
    public void initialize(){
    }

    @Override
    public void execute(){
        mPickUpSubsystem.pickUpBall();
    }

    @Override
    public void end(boolean interupted){
        mPickUpSubsystem.stopMotors();
    }

    @Override
    public boolean isFinished(){
        return false;
    }
}