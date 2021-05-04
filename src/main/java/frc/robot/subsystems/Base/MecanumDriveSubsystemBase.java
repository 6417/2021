package frc.robot.subsystems.base;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class MecanumDriveSubsystemBase extends SubsystemBase {
    public enum DriveMode {FieldOriented, RobotOriented};
    public void toggleDriveMode() {}
    public void drive(double xSpeed, double ySpeed, double zRotation) {}
    public void updateOdometry() {}
    public void resetOdometry() {}
    public Pose2d getPosition() {
    return new Pose2d((double)0, (double)0, new Rotation2d(0));
    };
}