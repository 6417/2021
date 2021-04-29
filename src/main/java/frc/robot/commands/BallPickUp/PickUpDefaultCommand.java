package frc.robot.commands.ballPickUp;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.PickUpSubsystem;
import frc.robot.subsystems.PickUpSubsystem.BallColor;

public class PickUpDefaultCommand extends CommandBase{
    BallColor currentColor;

    public PickUpDefaultCommand(){
    }

    @Override
    public void initialize(){
        System.out.println("hello");
    }

    @Override
    public void execute(){
        currentColor = PickUpSubsystem.getInstance().getBallColor();
        SmartDashboard.putString("BallColor", currentColor.toString());
    }
    
    @Override
    public void end(boolean interrupted){
    }

    @Override
    public boolean isFinished(){
        return false;
    }
}