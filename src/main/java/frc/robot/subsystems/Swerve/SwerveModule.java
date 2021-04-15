package frc.robot.subsystems.swerve;

import java.util.Optional;
import java.util.function.Supplier;

import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import frc.robot.utilities.PIDValues;
import frc.robot.utilities.Vector2d;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.LimitSwitchPolarity;
import frc.robot.utilities.swerveLimiter.SwerveLimiter;

public class SwerveModule implements Sendable {
    private boolean isEncoderZeroed = false;

    public static class Config implements Cloneable {
        public Supplier<FridolinsMotor> driveMotorInitializer;
        public Supplier<FridolinsMotor> rotationMotorInitializer;
        public PIDValues drivePID;
        public PIDValues rotationPID;
        public double rotationMotorTicksPerRotation;
        public double driveMotorTicksPerRotation;
        public double wheelCircumference; // in meter
        public Translation2d mountingPoint; // in meter
        public Supplier<SwerveLimiter> limiterInitializer;
        public double maxVelocity; // in drive motor encoder velocity units
        public FridolinsMotor.FeedbackDevice driveEncoderType;
        public FridolinsMotor.FeedbackDevice rotationEncoderType;
        public Optional<Boolean> driveSensorInverted = Optional.empty();
        public boolean driveMotorInverted;
        public double halSensorPosition;
        public boolean limitModuleStates;
        public boolean centricSwerve;
        public LimitSwitchPolarity limitSwitchPolarity;
        public double driveAcceleration;
        public double maxRotationVelocity;

        @Override
        public Config clone() {
            try {
                return (Config) super.clone();
            } catch (CloneNotSupportedException e) {
                Config copy = new Config();
                copy.driveMotorInitializer = driveMotorInitializer;
                copy.rotationMotorInitializer = rotationMotorInitializer;
                copy.drivePID = drivePID.clone();
                copy.rotationPID = rotationPID.clone();
                copy.rotationMotorTicksPerRotation = rotationMotorTicksPerRotation;
                copy.driveMotorTicksPerRotation = driveMotorTicksPerRotation;
                copy.wheelCircumference = wheelCircumference;
                copy.mountingPoint = new Translation2d(mountingPoint.getX(), mountingPoint.getY());
                copy.limiterInitializer = limiterInitializer;
                return copy;
            }
        }
    }

    private static class Motors {
        public FridolinsMotor drive;
        public FridolinsMotor rotation;
        public double rotationMotorTicksPerRotation;
        public double driveMotorTicksPerRotation;
        public double wheelCircumference;
        public double maxVelocity;
        public double maxDriveAcceleration;

        public Motors(FridolinsMotor drive, FridolinsMotor.FeedbackDevice driveEncoderType, boolean driveMotorInverted,
                Optional<Boolean> driveSensorInverted, FridolinsMotor rotation,
                FridolinsMotor.FeedbackDevice rotationEncoderType, LimitSwitchPolarity limitSwitchPolarity) {
            // DO NOT MAKE FACTORY DEFAULTS, for some reason it breaks every thing
            this.drive = drive;
            this.rotation = rotation;
            this.drive.configEncoder(driveEncoderType, (int) driveMotorTicksPerRotation);
            this.rotation.configEncoder(rotationEncoderType, (int) rotationMotorTicksPerRotation);
            driveSensorInverted.ifPresent(this.drive::setEncoderDirection);
            this.drive.setInverted(driveMotorInverted);
            this.rotation.enableForwardLimitSwitch(limitSwitchPolarity, true);
        }
    }

    private Motors motors;
    private SwerveLimiter limiter;
    private SwerveModuleState desiredState = new SwerveModuleState();
    public final double halSensorPosition;
    public final boolean centricSwerve;
    public final boolean limitedModuleStates;

    public SwerveModule(Config config) {
        motors = new Motors(config.driveMotorInitializer.get(), config.driveEncoderType, config.driveMotorInverted,
                config.driveSensorInverted, config.rotationMotorInitializer.get(), config.rotationEncoderType,
                config.limitSwitchPolarity);
        motors.drive.setPID(config.drivePID);
        motors.rotation.setPID(config.rotationPID);
        motors.driveMotorTicksPerRotation = config.driveMotorTicksPerRotation;
        motors.rotationMotorTicksPerRotation = config.rotationMotorTicksPerRotation;
        motors.wheelCircumference = config.wheelCircumference;
        limiter = config.limiterInitializer.get();
        motors.maxVelocity = config.maxVelocity;
        halSensorPosition = config.halSensorPosition;
        centricSwerve = config.centricSwerve;
        limitedModuleStates = config.limitModuleStates;
        motors.maxDriveAcceleration = config.driveAcceleration;
    }

    public Vector2d getModuleRotation() {
        return Vector2d.fromRad(getModuleRotationAngle());
    }

    public double getModuleRotationAngle() {
        return Vector2d
                .fromRad(((motors.rotation.getEncoderTicks() / motors.rotationMotorTicksPerRotation) * Math.PI * 2)
                        % (Math.PI * 2))
                .toRadians();
    }

    public double getRawModuleRotationAngle() {
        return (motors.rotation.getEncoderTicks() / motors.rotationMotorTicksPerRotation) * Math.PI * 2;
    }

    public Vector2d getTargetVector() {
        return Vector2d.fromRad(desiredState.angle.getRadians());
    }

    private double angleToRotationMotorEncoderTicks(double angle) {
        double angleDelta = Math.acos(getModuleRotation().dot(Vector2d.fromRad(angle)));
        double steeringDirection = Math.signum(getModuleRotation().cross(Vector2d.fromRad(angle))); // don't know why it
                                                                                                    // works, but it
                                                                                                    // works
        return motors.rotation.getEncoderTicks()
                + steeringDirection * (angleDelta / (Math.PI * 2)) * motors.rotationMotorTicksPerRotation;
    }

    private double meterPerSecondToDriveMotorEncoderVelocityUnits(double speedMs) {
        return (speedMs / motors.wheelCircumference) * motors.driveMotorTicksPerRotation;
    }

    private double driveMotorEncoderVelocityToPercent(double encoderSpeed) {
        return encoderSpeed / meterPerSecondToDriveMotorEncoderVelocityUnits(motors.maxVelocity);
    }

    public double getSpeed() {
        return motors.drive.getEncoderVelocity();
    }

    private static SwerveModuleState optimize(SwerveModuleState desiredState, Rotation2d currentAngle) {
        var delta = desiredState.angle.minus(currentAngle);
        if (Math.abs(delta.getDegrees()) > 90.0) {
            return new SwerveModuleState(-desiredState.speedMetersPerSecond,
                    desiredState.angle.rotateBy(Rotation2d.fromDegrees(180.0)));
        } else {
            return new SwerveModuleState(desiredState.speedMetersPerSecond, desiredState.angle);
        }
    }

    public void setDesiredState(SwerveModuleState state, double rotationOfsetFactor) {
        if (limitedModuleStates)
            desiredState = limiter.limitState(state, getModuleRotation(),
                    driveMotorEncoderVelocityToPercent(getSpeed()), rotationOfsetFactor);
        else {
            desiredState = limiter.limitState(state, getModuleRotation(), 0.0, 1.0 /* rotationOfsetFactor */);
            desiredState.speedMetersPerSecond = state.speedMetersPerSecond;
        }

        if (centricSwerve)
            desiredState = optimize(desiredState, new Rotation2d(getModuleRotationAngle()));
        else if (desiredState.speedMetersPerSecond < 0.0) {
            desiredState.angle.rotateBy(Rotation2d.fromDegrees(180));
            desiredState.speedMetersPerSecond *= -1;
        }
    }

    public void enableLimitSwitch() {
        motors.rotation.enableForwardLimitSwitch(LimitSwitchPolarity.kNormallyOpen, true);
    }

    public void disableLimitSwitch() {
        motors.rotation.enableForwardLimitSwitch(LimitSwitchPolarity.kNormallyOpen, false);
    }

    private double applyMaxAccelerationToDriveMotorVelocity(double desiredVelocity) {
        if (Math.abs(motors.drive.getEncoderVelocity() - desiredVelocity) > motors.maxDriveAcceleration) {
            return motors.drive.getEncoderVelocity()
                    + Math.signum(desiredVelocity - motors.drive.getEncoderVelocity()) * motors.maxDriveAcceleration;
        }
        return desiredVelocity;
    }

    public void drive(double speedFactor) {
        motors.rotation.setPosition(angleToRotationMotorEncoderTicks(desiredState.angle.getRadians()));
        motors.drive.setVelocity(applyMaxAccelerationToDriveMotorVelocity(
                meterPerSecondToDriveMotorEncoderVelocityUnits(desiredState.speedMetersPerSecond * speedFactor)));
    }

    public boolean isHalSensorTriggered() {
        return motors.rotation.isForwardLimitSwitchActive(); // TODO: check to which limit switch the hal sensor is
                                                             // connected to
    }

    public void rotateModule(double speed) {
        motors.rotation.set(speed);
    }

    public void stopDriveMotor() {
        motors.drive.set(0.0);
    }

    public void stopRotationMotor() {
        motors.rotation.set(0.0);
    }

    public double getRotationEncoderTicks() {
        return motors.rotation.getEncoderTicks();
    }

    public void setDesiredRotationMotorTicks(double position) {
        motors.rotation.setPosition(position);
    }

    public void stopAllMotors() {
        stopDriveMotor();
        stopRotationMotor();
    }

    public boolean hasEncoderBeenZeroed() {
        return isEncoderZeroed;
    }

    public void setCurrentRotationToEncoderHome() {
        isEncoderZeroed = true;
        motors.rotation.setEncoderPosition(0);
    }

    public void invertRotationDirection() {
        desiredState.angle.rotateBy(Rotation2d.fromDegrees(180));
    }

    public void setEncoderZeroedFalse() {
        isEncoderZeroed = false;
    }

    public SwerveModuleState getDesiredModuleState() {
        return desiredState;
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.addDoubleProperty("Desired state speed", () -> desiredState.speedMetersPerSecond, null);
        builder.addDoubleProperty("Desired state speed encoder velocity units",
                () -> applyMaxAccelerationToDriveMotorVelocity(
                        meterPerSecondToDriveMotorEncoderVelocityUnits(desiredState.speedMetersPerSecond)),
                null);
        builder.addDoubleProperty("Desired state angle", () -> desiredState.angle.getDegrees(), null);
        builder.addDoubleProperty("Desired state rotation encoder ticks",
                () -> angleToRotationMotorEncoderTicks(desiredState.angle.getRadians()), null);
        builder.addDoubleProperty("Module angel", () -> getModuleRotationAngle() * 360 / (Math.PI * 2), null);
        builder.addDoubleProperty("Moudle speed", () -> getSpeed(), null);
        builder.addDoubleProperty("Module Rotation Encoder Ticks", motors.rotation::getEncoderTicks, null);
        builder.addBooleanProperty("forward limit switch", motors.rotation::isForwardLimitSwitchActive, null);
        builder.addBooleanProperty("Module Zeroed", this::hasEncoderBeenZeroed, null);
    }

    public void setRotationEncoderTicks(double ticks) {
        isEncoderZeroed = true;
        motors.rotation.setEncoderPosition(ticks);
    }
}