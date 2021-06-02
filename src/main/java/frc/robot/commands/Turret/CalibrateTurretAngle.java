/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands.Turret;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.subsystems.TurretSubsystem;
import frc.robot.subsystems.base.TurretSubsystemBase;

/**
 * An example command that uses an example subsystem.
 */
public class CalibrateTurretAngle extends CommandBase {
  private final TurretSubsystemBase throwerSubsystem;

  public CalibrateTurretAngle() {
    throwerSubsystem = TurretSubsystem.getInstance();
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {

  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    throwerSubsystem.runShootingAngleMotor(Constants.Turret.ANGLE_CALIBRATION_SPEED);
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    throwerSubsystem.runShootingAngleMotor(0);
    throwerSubsystem.setShootingAngleEncoderPosition(0);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return throwerSubsystem.getShootingAngleMotorLimitSwitch();
  }
}
