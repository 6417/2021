package frc.robot.subsystems.Swerve;

import java.util.function.Supplier;

import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import frc.robot.utilities.PIDValues;
import frc.robot.utilities.Vector2d;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;

public class SwerveModule {
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

        public Motors(FridolinsMotor drive, FridolinsMotor rotation) {
            this.drive = drive;
            this.rotation = rotation;
        }
    }

    private Motors motors;

    public SwerveModule(Config config) {
        motors = new Motors(config.driveMotorInitializer.get(), config.rotationMotorInitializer.get());
        motors.drive.setPID(config.drivePID);
        motors.rotation.setPID(config.rotationPID);
        motors.driveMotorTicksPerRotation = config.driveMotorTicksPerRotation;
        motors.rotatoinMotorTicksPerRotation = config.rotationMotorTicksPerRotation;
        motors.wheelCircumference = config.wheelCircumference;
    }

    public Vector2d getModuleRotation() {
        return Vector2d.fromRad(getModuleRotationAngle());
    }

    public double getModuleRotationAngle() {
        return (motors.rotation.getEncoderTicks() / motors.rotatoinMotorTicksPerRotation) * Math.PI * 2;
    }

    private double angleToRotationMotorEncoderTicks(double angle) {
        return angle / (Math.PI * 2) * motors.rotatoinMotorTicksPerRotation;
    }

    private double meterPerSecondToDriveMotorEncoderTicksPer100ms(double speedMs) {
        return (speedMs / motors.wheelCircumference) * motors.driveMotorTicksPerRotation * 10;
    }

    public void setDesiredState(SwerveModuleState desiredState) {
        SwerveModuleState.optimize(desiredState, new Rotation2d(getModuleRotationAngle()));
        motors.rotation.setPosition(angleToRotationMotorEncoderTicks(desiredState.angle.getRadians()));
        motors.drive.setVelocity(meterPerSecondToDriveMotorEncoderTicksPer100ms(desiredState.speedMetersPerSecond));
    }

    public boolean isHalSensorTriggered() {
        return motors.rotation.isForwardLimitSwitchActive(); // TODO: check to which limit switch the hal sensor is connected to
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
}