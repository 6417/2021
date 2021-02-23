package frc.robot.utilities;

import frc.robot.utilities.fridolinsMotor.FridolinsMotor;

public interface MotorInitializer {
    public FridolinsMotor initialize();

    // public static class FridolinsTalonSRXInitializer implements MotorInitializer {
    //     int id; 
    //     public FridolinsTalonSRXInitializer(int id) {
    //         this.id = id; 
    //     }

    //     @Override
    //     public FridolinsMotor initialize() {
    //         return new 
    //     }
    // }
}