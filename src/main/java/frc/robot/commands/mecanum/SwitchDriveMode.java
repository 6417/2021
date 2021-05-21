package frc.robot.commands.mecanum;

import edu.wpi.first.wpilibj2.command.CommandBase;

public class SwitchDriveMode extends CommandBase{
    public SwitchDriveMode() {

    }

    @Override
    public void initialize() {
        DefaultDriveCommand.tankdriveModeActivated = !DefaultDriveCommand.tankdriveModeActivated;
    }
     
    @Override
    public boolean isFinished() {
        return true;
    }
}