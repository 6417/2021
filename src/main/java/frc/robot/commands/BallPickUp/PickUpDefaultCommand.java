package frc.robot.commands.ballPickUp;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.PickUpSubsystem;

public class PickUpDefaultCommand extends CommandBase{

    public PickUpDefaultCommand(){
    }

    @Override
    public void initialize(){
    }

    @Override
    public void execute(){
        if(PickUpSubsystem.ballInTunnel){
            PickUpSubsystem.getInstance().putColorInDashBoard();
        }
    }

    @Override
    public void end(boolean interrupted){
    }

    @Override
    public boolean isFinished(){
        return false;
    }
}