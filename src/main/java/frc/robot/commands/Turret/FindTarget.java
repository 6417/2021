package frc.robot.commands.Turret;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.subsystems.TurretSubsystem;
import frc.robot.subsystems.base.TurretSubsystemBase;
import frc.robot.utilities.VisionService;
import frc.robot.utilities.VisionService.Values;
import frc.robot.utilities.baseClasses.VisionServiceBase;

public class FindTarget extends CommandBase {
  private final TurretSubsystemBase throwerSubsystem;
  private final VisionServiceBase vision;
  private Values values;
  private int turningDirection;

  /**
   * Creates a new ExampleCommand.
   *
   * @param subsystem The subsystem used by this command.
   */
  public FindTarget() {
    this.throwerSubsystem = TurretSubsystem.getInstance();
    this.vision = VisionService.getInstance();
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    values = vision.getValues();
    turningDirection = -1;
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    if (throwerSubsystem.getCurrentTurretShootingDirection() < -180) {
      turningDirection = 1;
    }

    values = vision.getValues();
    throwerSubsystem.setTurretShootingDirection(throwerSubsystem.getCurrentTurretShootingDirection()
        + Constants.Turret.AIMING_ANGLE_INCREMENT * turningDirection);
    System.out.println(throwerSubsystem.getCurrentTurretShootingDirection());
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    throwerSubsystem.setTurretShootingDirection(throwerSubsystem.getCurrentTurretShootingDirection()
        + Constants.Turret.AIMING_ANGLE_INCREMENT * -turningDirection);
    throwerSubsystem.runShootingDirectionMotor(0);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    // return this.values.targetInView == true || (turningDirection == 1 &&
    // throwerSubsystem.getCurrentTurretShootingDirection() >= -5);
    return this.values.targetInView
        || throwerSubsystem.getCurrentTurretShootingDirection() >= -5 && turningDirection == 1;
  }
}