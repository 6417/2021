package frc.robot.subsystems.Swerve;

import java.util.function.Supplier;

import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SendableRegistry;
import frc.robot.utilities.CSVLogger;
import frc.robot.utilities.PIDValues;
import frc.robot.utilities.Vector2d;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;
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
        public boolean driveSensorInverted;
        public boolean driveMotorInverted;

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
        public double rotatoinMotorTicksPerRotation;
        public double driveMotorTicksPerRotation;
        public double wheelCircumference;
        public double maxVelocity;

        public Motors(FridolinsMotor drive, FridolinsMotor.FeedbackDevice driveEncoderType, boolean driveMotorInverted,
                boolean driveSensorInverted, FridolinsMotor rotation,
                FridolinsMotor.FeedbackDevice rotationEncoderType) {
            this.drive = drive;
            this.rotation = rotation;
            this.drive.configEncoder(driveEncoderType, (int) driveMotorTicksPerRotation);
            this.rotation.configEncoder(rotationEncoderType, (int) rotatoinMotorTicksPerRotation);
            this.drive.setEncoderDirection(driveSensorInverted);
            this.drive.setDirection(driveMotorInverted);
        }
    }

    private Motors motors;
    private SwerveLimiter limiter;
    private SwerveModuleState desiredState = new SwerveModuleState();
    public CSVLogger csvLogger;

    public SwerveModule(Config config) {
        motors = new Motors(config.driveMotorInitializer.get(), config.driveEncoderType, config.driveMotorInverted,
                config.driveSensorInverted, config.rotationMotorInitializer.get(), config.rotationEncoderType);
        motors.drive.setPID(config.drivePID);
        motors.rotation.setPID(config.rotationPID);
        motors.driveMotorTicksPerRotation = config.driveMotorTicksPerRotation;
        motors.rotatoinMotorTicksPerRotation = config.rotationMotorTicksPerRotation;
        motors.wheelCircumference = config.wheelCircumference;
        limiter = config.limiterInitializer.get();
        motors.maxVelocity = config.maxVelocity;
    }

    public Vector2d getModuleRotation() {
        return Vector2d.fromRad(getModuleRotationAngle());
    }

    public double getModuleRotationAngle() {
        return ((motors.rotation.getEncoderTicks() / motors.rotatoinMotorTicksPerRotation) * Math.PI * 2)
                % (Math.PI * 2);
    }

    public Vector2d getTargetVector() {
        return Vector2d.fromRad(desiredState.angle.getRadians());
    }

    private double angleToRotationMotorEncoderTicks(double angle) {
        return angle / (Math.PI * 2) * motors.rotatoinMotorTicksPerRotation;
    }

    private double meterPerSecondToDriveMotorEncoderVelocityUnits(double speedMs) {
        return (speedMs / motors.wheelCircumference) * motors.driveMotorTicksPerRotation;
    }

    private double driveMotorEncoderVelocityToPercent(double encoderSpeed) {
        return encoderSpeed / motors.maxVelocity;
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

    public void setDesiredState(SwerveModuleState desiredState) {
        // this.desiredState = limiter.limitState(desiredState, getModuleRotation(),
        // driveMotorEncoderVelocityToPercent(getSpeed()));
        this.desiredState = optimize(desiredState, new Rotation2d(getModuleRotationAngle()));
        csvLogger.put("desired state angle", desiredState.angle.getDegrees());
        csvLogger.put("desired rotation motor encoder ticks",
                angleToRotationMotorEncoderTicks(desiredState.angle.getRadians()));
        csvLogger.put("desired state speed", desiredState.speedMetersPerSecond);
        csvLogger.put("rotation angle", getModuleRotationAngle());
        csvLogger.put("rotation encoder ticks", motors.rotation.getEncoderTicks());
    }

    public void drive() {
        motors.rotation.setPosition(angleToRotationMotorEncoderTicks(desiredState.angle.getRadians())); // TODO: Fix bug
                                                                                                        // that module
                                                                                                        // rotates to
                                                                                                        // many times
        motors.drive.setVelocity(meterPerSecondToDriveMotorEncoderVelocityUnits(desiredState.speedMetersPerSecond));
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

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.addDoubleProperty("Desired state speed", () -> desiredState.speedMetersPerSecond, null);
        builder.addDoubleProperty("Desired state speed encoder velocity units",
                () -> meterPerSecondToDriveMotorEncoderVelocityUnits(desiredState.speedMetersPerSecond), null);
        builder.addDoubleProperty("Desired state angle", () -> desiredState.angle.getDegrees(), null);
        builder.addDoubleProperty("Module angel", () -> getModuleRotationAngle() * 360 / (Math.PI * 2), null);
        builder.addDoubleProperty("Moudle speed", () -> getSpeed(), null);
    }
}