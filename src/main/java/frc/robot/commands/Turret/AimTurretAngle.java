package frc.robot.commands.Turret;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.TurretSubsystem;
import frc.robot.subsystems.base.TurretSubsystemBase;
import frc.robot.utilities.VisionService;
import frc.robot.utilities.baseClasses.VisionServiceBase;

public class AimTurretAngle extends CommandBase {
    private final TurretSubsystemBase thrower;
    private final VisionServiceBase vision;
    private VisionService.Values values;
    private double setPoint;

    public AimTurretAngle() {
        thrower = TurretSubsystem.getInstance();
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
        return Math.abs(TurretSubsystem.getInstance().getShootingAngleMotorEncoderTicks() - setPoint) < 1;
    }

    @Override
    public void end(boolean interrupted) {
        thrower.runShootingAngleMotor(0);
    }
}
