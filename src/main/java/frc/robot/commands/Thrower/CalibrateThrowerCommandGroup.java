package frc.robot.commands.Thrower;

import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import frc.robot.subsystems.ThrowerSubsystem;

public class CalibrateThrowerCommandGroup extends ParallelCommandGroup{

    public CalibrateThrowerCommandGroup() {
        addCommands(new CalibrateShootingAngleCommand(), new CalibrateTurretShootingDirectionCommand());
        addRequirements(ThrowerSubsystem.getInstance());
    }
}
