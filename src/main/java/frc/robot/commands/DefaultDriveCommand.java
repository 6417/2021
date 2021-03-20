package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.MecanumDriveSubsystem;
import frc.robot.utilities.Controller;

public class DefaultDriveCommand extends CommandBase {
    public DefaultDriveCommand() {
        addRequirements(MecanumDriveSubsystem.getInstance());
    }

    @Override
    public void execute() {
        MecanumDriveSubsystem.getInstance().drive(Controller.getInstance().driveJoystick.getLeftStickX(),
                Controller.getInstance().driveJoystick.getLeftStickY(), 
                Controller.getInstance().driveJoystick.getRightStickX());
    }
}