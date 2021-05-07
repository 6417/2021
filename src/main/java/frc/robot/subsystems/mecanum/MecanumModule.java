package frc.robot.subsystems.mecanum;

import java.util.function.Supplier;

import frc.robot.utilities.fridolinsMotor.FridolinsMotor;

public class MecanumModule {
    public static class Config {
        public boolean motorInverted;
        public boolean encoderInverted;
        public Supplier<FridolinsMotor> motorInitializer;
    }
}