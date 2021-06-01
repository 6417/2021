/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands.Thrower;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.ThrowerSubsystem;
import frc.robot.subsystems.base.ThrowerSubsystemBase;
import frc.robot.utilities.VisionService;
import frc.robot.utilities.baseClasses.VisionServiceBase;

/**
 * An example command that uses an example subsystem.
 */
public class SetTurretShootingDirectionCommand extends CommandBase {
  @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
  private final ThrowerSubsystemBase throwerSubsystem;
  private final VisionServiceBase vision;
  private double angle;
  private VisionService.Values values;
  private long stallStartTime;
  private long accelerationStart;


  /**
   * Creates a new ExampleCommand.
   *
   * @param subsystem The subsystem used by this command.
   */
  public SetTurretShootingDirectionCommand() {
    this.throwerSubsystem = ThrowerSubsystem.getInstance();
    this.vision = VisionService.getInstance();
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    this.values = vision.getValues();
    this.angle = throwerSubsystem.calculateTurretDirection(values);
    this.accelerationStart = System.currentTimeMillis();
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
      if(values.targetInView){
        if (Math.abs(angle) >= 45) {
          throwerSubsystem.setTurretShootingDirection(angle);
        }
      }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
      throwerSubsystem.runShootingDirectionMotor(0);
  }


  @Override
  public boolean isFinished() {
    if (System.currentTimeMillis() - accelerationStart <= 100) {
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