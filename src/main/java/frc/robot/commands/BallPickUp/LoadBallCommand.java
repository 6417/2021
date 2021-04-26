package frc.robot.commands.ballPickUp;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.PickUpSubsystem;
import frc.robot.utilities.Timer;

public class LoadBallCommand extends CommandBase{

    Timer timer;

    public LoadBallCommand(){
        addRequirements(PickUpSubsystem.getInstance());
        timer = new Timer(System::currentTimeMillis);
    }

    @Override
    public void initialize(){
        timer.start();
    }

    @Override
    public void execute(){
        PickUpSubsystem.getInstance().loadBall();
    }

    @Override
    public void end(boolean interupted){
        PickUpSubsystem.getInstance().stopMotors();
    }

    @Override
    public boolean isFinished(){
        if(timer.getPastTime().get() < 300){
            return false;
        }
        else{
            return true;
        }
    }
}