package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Robot;

public class ZeroNavx extends CommandBase {
    public ZeroNavx() {

    }

    @Override
    public void initialize() {
        Robot.getNavx().reset();
    }
}