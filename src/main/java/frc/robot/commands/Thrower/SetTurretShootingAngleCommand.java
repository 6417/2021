package frc.robot.commands.Thrower;

import java.util.Optional;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.ThrowerSubsystem;
import frc.robot.subsystems.Base.ThrowerSubsystemBase;
import frc.robot.utilities.VisionService;
import frc.robot.utilities.baseClasses.VisionServiceBase;

public class SetTurretShootingAngleCommand extends CommandBase {
    private final ThrowerSubsystemBase thrower;
    private final VisionServiceBase vision;
    private VisionService.Values values;
    private double encoderTicks;

    public SetTurretShootingAngleCommand() {
        thrower = ThrowerSubsystem.getInstance();
        vision = VisionService.getInstance();
        encoderTicks = 0;
    }

    @Override
    public void initialize() {
        
    }

    @Override
    public void execute() {
        values = vision.getValues();
        thrower.setTurretShootingAngle(thrower.calculateTurretAngleTicks(values));
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void end(boolean interrupted) {
        thrower.runShootingAngleMotor(0);
    }
}
