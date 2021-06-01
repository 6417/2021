package frc.robot.subsystems.base;

import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ThrowerSubsystemBase extends SubsystemBase{
  public double getShootingDirectionMotorSpeed() {return 0;}
  public double getShootingDirectionMotorEncoderTicks() {return 0;}
  public double getShootingAngleMotorSpeed() {return 0;}
  public double getCurrentTurretShootingDirection() {return 0;}
  public boolean getShootingAngleMotorLimitSwitch() {return false;}
  public double getShootingAngleMotorEncoderTicks() {return 0;}
  public void runLoaderMotor(double speed) {}
  public void runShootingDirectionMotor(double speed) {}
  public void runShootingAngleMotor(double speed) {}
  public void runShooter(double speed) {}
  public void setTurretShootingDirection(double angle) {}
  public void setTurretShootingAngle(double angle) {}
  public void setShooterSpeed(double speed) {}
  public void setDirectionEncoderPosition(double position) {}
  public void setShootingAngleEncoderPosition(double position) {}
  public void initMotors() {}

  public double calculateTurretDirection(frc.robot.utilities.VisionService.Values values) {return 0;}
  public double calculateTurretAngleTicks(frc.robot.utilities.VisionService.Values values) {return 0;}
}
