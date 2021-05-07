package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Controller;
import frc.robot.subsystems.mecanum.MecanumDriveSubsystem;

public class DefaultDriveCommand extends CommandBase {
    public DefaultDriveCommand() {
        addRequirements(MecanumDriveSubsystem.getInstance());
    }

    @Override
    public void execute() {
        MecanumDriveSubsystem.getInstance().drive(-Controller.getInstance().driveJoystick.getLeftStickX(),
                Controller.getInstance().driveJoystick.getLeftStickY(), 
                Controller.getInstance().driveJoystick.getRightStickX());
        System.out.println(Controller.getInstance().driveJoystick.getRightStickX());
    }
}