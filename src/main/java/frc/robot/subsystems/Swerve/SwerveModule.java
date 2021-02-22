package frc.robot.subsystems.Swerve;

import frc.robot.utilities.fridolinsMotor.FridolinsMotor;

public class SwerveModule {
    private static class Motors {
        public FridolinsMotor drive;
        public FridolinsMotor rotation;

        public Motors(FridolinsMotor drive, FridolinsMotor rotation) {
            this.drive = drive;
            this.rotation = rotation;
        }
    }

    private Motors motors;

    public SwerveModule(FridolinsMotor driveMotor, FridolinsMotor rotationMotor) {
        motors = new Motors(driveMotor, rotationMotor);
    }
}