package frc.robot.subsystems.Swerve;

import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import frc.robot.utilities.MotorInitializer;
import frc.robot.utilities.PIDValues;
import frc.robot.utilities.Vector2d;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;

public class SwerveModule {
    public static enum MountingLocation {
        FrontRight, FrontLeft, BackRight, BackLeft  
    }

    public static class Config {
        public MotorInitializer driveMotorInitializer;
        public MotorInitializer rotationMotorInitializer;
        public PIDValues drivePID;
        public PIDValues rotationPID;
        public double rotationMotorTicksPerRotation;
        public double driveMotorTicksPerRotation;
        public double wheelCircumference; // in meter
        public Translation2d mountingPoint; // in meter
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
        motors = new Motors(config.driveMotorInitializer.initialize(), config.rotationMotorInitializer.initialize());
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
}