package frc.robot.commands.PickUp;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.PickUpSubsystem;
import frc.robot.utilities.LatchedBoolean;

public class BallPickUpCommand extends CommandBase {

    LatchedBoolean latchedBoolean;

    public BallPickUpCommand() {
       addRequirements(PickUpSubsystem.getInstance()); 
    }

    @Override
    public void initialize(){
        latchedBoolean = new LatchedBoolean(LatchedBoolean.EdgeDetection.FALLING);
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
        return latchedBoolean.update(PickUpSubsystem.getInstance().getLightBarrier());
    }
}