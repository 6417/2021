/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;


import frc.robot.Constants;
import frc.robot.subsystems.Base.ThrowerSubsystemBase;
import frc.robot.utilities.fridolinsMotor.FridoCANSparkMax;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.LimitSwitchPolarity;

public class ThrowerSubsystem extends ThrowerSubsystemBase {

  private static ThrowerSubsystemBase instance;
  private FridolinsMotor loaderMotor;
  private FridoCANSparkMax turretDirectionMotor;
  private FridolinsMotor turretAngleMotor;
  private FridolinsMotor shootMotor;
  
  public ThrowerSubsystem() {
    loaderMotor = Constants.Thrower.Motors.loaderMotor.get();
    turretDirectionMotor = (FridoCANSparkMax)Constants.Thrower.Motors.directionMotor.get();
    turretAngleMotor = Constants.Thrower.Motors.angleMotor.get();
    shootMotor = Constants.Thrower.Motors.shootMotor.get();

    initMotors();
  }

  public static ThrowerSubsystemBase getInstance() {
    if (instance == null) {
        if (Constants.Thrower.IS_ENABLED) 
            instance = new ThrowerSubsystem();
        else {
          instance = new ThrowerSubsystemBase();
        }
    }
    return instance;
  }

  public void initMotors() {
    loaderMotor.factoryDefault();
    turretDirectionMotor.factoryDefault();
    turretAngleMotor.factoryDefault();
    shootMotor.factoryDefault();

    loaderMotor.enableForwardLimitSwitch(LimitSwitchPolarity.kNormallyClosed, false);
    loaderMotor.enableReverseLimitSwitch(LimitSwitchPolarity.kNormallyClosed, false);

    turretDirectionMotor.selectBuiltinFeedbackSensor();

    turretDirectionMotor.setPID(Constants.Thrower.PIDControllers.DirectionMotor.values);
    turretDirectionMotor.setEncoderPosition(0);
  }

  private double convertTurretAngleToEncoderTicks(double angle) {
    angle = (angle / 360) * Constants.Thrower.GEAR_RATIO_TURRET_DIRECTION;
    return angle;
  }

  private double convertEncoderTicksToTurretAngle(double encoderTicks) {
    return 360 * ((turretDirectionMotor.getEncoderTicks() % Constants.Thrower.GEAR_RATIO_TURRET_DIRECTION) / Constants.Thrower.GEAR_RATIO_TURRET_DIRECTION); 
  }

  private double convertShootingAngleToEncoderTicks(double angle) {
    return 0;
  }

  private boolean getBallAcquiredLightBarrier() {
    return loaderMotor.isForwardLimitSwitchActive(); 
  }

  private boolean getBallLoadedLightBarrier() {
    return loaderMotor.isReverseLimitSwitchActive();
  }

  @Override
  public void runLoaderMotor(double speed) {
    loaderMotor.set(speed);
  }

  @Override
  public void setTurretShootingDirection(double angle) {
    turretDirectionMotor.setPosition(convertTurretAngleToEncoderTicks(angle));
    System.out.println("Set the position to " + convertTurretAngleToEncoderTicks(angle) + "    " + turretDirectionMotor.getEncoderTicks());
  }

  @Override
  public void runTurretShootingAngleMotor(double percent) {
    turretAngleMotor.set(percent);
  }
  
  @Override
  public void setTurretShootingAngle(double angle) {
    turretAngleMotor.setPosition(convertTurretAngleToEncoderTicks(angle));    
  }

  @Override
  public void runShooterMotor(double speed) {
    shootMotor.set(speed);
  }

  @Override
  public void setShooterSpeed(double speed) {
    shootMotor.setVelocity(speed);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}

