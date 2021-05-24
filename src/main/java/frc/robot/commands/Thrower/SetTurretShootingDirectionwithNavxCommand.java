package frc.robot.commands.Thrower;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.Robot;
import frc.robot.subsystems.ThrowerSubsystem;
import frc.robot.subsystems.base.ThrowerSubsystemBase;

public class SetTurretShootingDirectionwithNavxCommand extends CommandBase{
  private final ThrowerSubsystemBase throwerSubsystem;
  private long stallStartTime;
  private long accelerationStart;

  /**
   * Creates a new ExampleCommand.
   *
   * @param subsystem The subsystem used by this command.
   */
  public SetTurretShootingDirectionwithNavxCommand() {
    this.throwerSubsystem = ThrowerSubsystem.getInstance();
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
      accelerationStart = System.currentTimeMillis();
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    throwerSubsystem.setTurretShootingDirection(Robot.getNavx().getAngle() - 180 + Constants.Thrower.ROBOT_START_OFFSET);
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
      throwerSubsystem.runShootingDirectionMotor(0);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    if (System.currentTimeMillis() - accelerationStart <= 200) {
        return false;
    }

    if (Math.abs(throwerSubsystem.getShootingDirectionMotorSpeed()) >= 2) {
        stallStartTime = 0;
        return false;
    }

    if (System.currentTimeMillis() - stallStartTime >= 200 && stallStartTime != 0) {
        return true;
    }
    
    if(Math.abs(throwerSubsystem.getShootingDirectionMotorSpeed()) <= 2 && stallStartTime == 0) {
        stallStartTime = System.currentTimeMillis();
        return false;
    }
    return true;
  }
}