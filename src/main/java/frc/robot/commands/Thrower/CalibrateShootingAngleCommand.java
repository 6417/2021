/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands.Thrower;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.ThrowerSubsystem;
import frc.robot.subsystems.Base.ThrowerSubsystemBase;
import frc.robot.utilities.Timer;

/**
 * An example command that uses an example subsystem.
 */
public class CalibrateShootingAngleCommand extends CommandBase {
  @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
  private final ThrowerSubsystemBase throwerSubsystem;
  private long stallStartTime;
  private long accelerationStart;


  /**
   * Creates a new ExampleCommand.
   *
   * @param subsystem The subsystem used by this command.
   */
  public CalibrateShootingAngleCommand() {
    throwerSubsystem = ThrowerSubsystem.getInstance();
    stallStartTime = 0;
    accelerationStart = System.currentTimeMillis();
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    stallStartTime = 0;
    accelerationStart = System.currentTimeMillis();
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    throwerSubsystem.runShootingAngleMotor(-0.1);;
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
    if (System.currentTimeMillis() - accelerationStart <= 200) {
      return false;
    }

    if (throwerSubsystem.getShootingAngleMotorSpeed() <= -800) {
      stallStartTime = 0;
      return false;
    }

    if (System.currentTimeMillis() - stallStartTime >= 100 && stallStartTime != 0) {
      return true;
    }
    
    if(throwerSubsystem.getShootingAngleMotorSpeed() >= -800 && stallStartTime == 0) {
      stallStartTime = System.currentTimeMillis();
      return false;
    }

    return true;
  }
}
