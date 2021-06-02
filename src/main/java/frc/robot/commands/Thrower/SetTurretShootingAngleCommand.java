package frc.robot.commands.Thrower;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.ThrowerSubsystem;
import frc.robot.subsystems.base.ThrowerSubsystemBase;
import frc.robot.utilities.VisionService;
import frc.robot.utilities.baseClasses.VisionServiceBase;

public class SetTurretShootingAngleCommand extends CommandBase {
    private final ThrowerSubsystemBase thrower;
    private final VisionServiceBase vision;
    private VisionService.Values values;
    private double setPoint;

    public SetTurretShootingAngleCommand() {
        thrower = ThrowerSubsystem.getInstance();
        vision = VisionService.getInstance();
    }

    @Override
    public void initialize() {
        values = vision.getValues();
    }

    @Override
    public void execute() {
        if (values.targetInView)
        {
            setPoint = thrower.calculateTurretAngleTicks(values);
            thrower.setTurretShootingAngle(setPoint);
        }
    }

    @Override
    public boolean isFinished() {
        return Math.abs(ThrowerSubsystem.getInstance().getShootingAngleMotorEncoderTicks() - setPoint) < 1;
    }

    @Override
    public void end(boolean interrupted) {
        thrower.runShootingAngleMotor(0);
    }
}
