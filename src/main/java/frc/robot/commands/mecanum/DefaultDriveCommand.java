package frc.robot.commands.mecanum;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Controller;
import frc.robot.subsystems.mecanum.MecanumDriveSubsystem;

public class DefaultDriveCommand extends CommandBase {
    public static boolean tankdriveModeActivated = false;

    public DefaultDriveCommand() {
        addRequirements(MecanumDriveSubsystem.getInstance());
    }

    private void tankDriveMode() {
            MecanumDriveSubsystem.getInstance().drive(0, Controller.getInstance().driveJoystick.getLeftStickY(),
                    Controller.getInstance().driveJoystick.getRightStickX());
    }

    private void mecanumDriveMode() {
            MecanumDriveSubsystem.getInstance().drive(Controller.getInstance().driveJoystick.getLeftStickX(),
                    Controller.getInstance().driveJoystick.getLeftStickY(),
                    Controller.getInstance().driveJoystick.getRightStickX());
    }

    @Override
    public void execute() {
        if (!tankdriveModeActivated)
            mecanumDriveMode();
        else
            tankDriveMode();
    }
}