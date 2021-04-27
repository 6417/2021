/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants;
import frc.robot.subsystems.base.ThrowerSubsystemBase;
import frc.robot.utilities.VisionService.Values;
import frc.robot.utilities.fridolinsMotor.FridoCANSparkMax;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.IdleModeType;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.LimitSwitchPolarity;

public class ThrowerSubsystem extends ThrowerSubsystemBase {

  private static ThrowerSubsystemBase instance;
  private FridolinsMotor loaderMotor;
  private FridoCANSparkMax turretDirectionMotor;
  private FridoCANSparkMax turretAngleMotor;
  private FridoCANSparkMax shootMotor;
  
  public ThrowerSubsystem() {
    loaderMotor = Constants.Thrower.Motors.loaderMotor.get();
    turretDirectionMotor = (FridoCANSparkMax)Constants.Thrower.Motors.directionMotor.get();
    turretAngleMotor = (FridoCANSparkMax)Constants.Thrower.Motors.angleMotor.get();
    shootMotor = (FridoCANSparkMax)Constants.Thrower.Motors.shootMotor.get();

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

  @Override
  public void initMotors() {
    loaderMotor.factoryDefault();
    turretDirectionMotor.factoryDefault();
    turretAngleMotor.factoryDefault();
    shootMotor.factoryDefault();

    shootMotor.selectBuiltinFeedbackSensor();
    shootMotor.setPID(Constants.Thrower.PIDControllers.ShooterMotor.values);

    loaderMotor.enableForwardLimitSwitch(LimitSwitchPolarity.kNormallyClosed, false);
    loaderMotor.enableReverseLimitSwitch(LimitSwitchPolarity.kNormallyClosed, false);

    turretDirectionMotor.selectBuiltinFeedbackSensor();
    turretDirectionMotor.setInverted(false);
    turretDirectionMotor.setIdleMode(IdleModeType.kBrake);

    turretAngleMotor.selectBuiltinFeedbackSensor();
    turretAngleMotor.setInverted(false);
    turretAngleMotor.setEncoderPosition(0);
    turretAngleMotor.enableForwardLimitSwitch(LimitSwitchPolarity.kNormallyClosed, true);
    turretAngleMotor.setPID(Constants.Thrower.PIDControllers.AngleMotor.values);

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

  @Override
  public double getShootingDirectionMotorSpeed() {
    return turretDirectionMotor.getEncoder().getVelocity();
  }

  @Override
  public double getShootingAngleMotorSpeed() {
    return turretAngleMotor.getEncoder().getVelocity();
  }

  @Override
  public double getCurrentTurretShootingDirection() {
    return convertEncoderTicksToTurretAngle(turretDirectionMotor.getEncoderTicks());
  }

  @Override
  public boolean getShootingAngleMotorLimitSwitch() {
    return turretAngleMotor.isForwardLimitSwitchActive();
  }

  @Override
  public double getShootingAngleMotorEncoderTicks() {
    return turretAngleMotor.getEncoderTicks();
  }

  @Override
  public void runLoaderMotor(double speed) {
    loaderMotor.set(speed);
  }

  @Override
  public void runShootingDirectionMotor(double speed) {
    turretDirectionMotor.set(speed);
  }

  @Override
  public void runShootingAngleMotor(double speed) {
    turretAngleMotor.set(speed);
  }

  @Override
  public void runShooter(double speed) {
    shootMotor.set(speed);
  }

  @Override
  public void calibrateShootingDirection() {
    
  }

  @Override
  public void setTurretShootingDirection(double angle) {
    turretDirectionMotor.setPosition(convertTurretAngleToEncoderTicks(angle));
    System.out.println("Set the position to " + convertTurretAngleToEncoderTicks(angle) + "    " + turretDirectionMotor.getEncoderTicks());
  }

  @Override
  public void setDirectionEncoderPosition(double position) {
    turretDirectionMotor.setEncoderPosition(position);
  }

  @Override
  public void setShootingAngleEncoderPosition(double position) {
    turretAngleMotor.setEncoderPosition(position);
  }

  @Override
  public void setTurretShootingAngle(double angle) {
    turretAngleMotor.setPosition(angle);    
  }

  @Override
  public void setShooterSpeed(double speed) {
    shootMotor.setVelocity(speed);
  }

  @Override
  public void periodic() {
  }

  @Override
  public double calculateTurretAngleTicks(Values values) {
    return 0.954845 * values.stripeHeight - 225.326;
    //return 0.000316 * Math.pow(values.stripeHeight, 3) - 0.088683 * Math.pow(values.stripeHeight, 2) + 9.09257 * values.stripeHeight - 496.119;
  }  

  @Override
  public double calculateTurretDirection(Values values) {
    return convertEncoderTicksToTurretAngle(turretDirectionMotor.getEncoderTicks()) + values.robotAngle ;
  }
  
  @Override
  public void initSendable(SendableBuilder builder) {
    builder.addDoubleProperty("AngleMotor encoder", () -> turretAngleMotor.getEncoderTicks(), null);
    builder.addDoubleProperty("ShootMotorSpeed", () -> shootMotor.getEncoderVelocity(), null);
    super.initSendable(builder);
  }
}
