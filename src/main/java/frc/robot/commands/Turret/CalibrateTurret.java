package frc.robot.commands.Turret;

import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

public class CalibrateTurret extends ParallelCommandGroup{

    public CalibrateTurret() {
        addCommands(new CalibrateTurretAngle(), new CalibrateTurretDirection());
    }
}
