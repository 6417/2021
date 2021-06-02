package frc.robot.commands.Thrower;

import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

public class CalibrateThrowerCommandGroup extends ParallelCommandGroup{

    public CalibrateThrowerCommandGroup() {
        addCommands(new CalibrateShootingAngleCommand(), new CalibrateTurretShootingDirectionCommand());
    }
}
