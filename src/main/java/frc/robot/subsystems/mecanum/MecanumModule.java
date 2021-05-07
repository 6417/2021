package frc.robot.subsystems.mecanum;

import java.util.function.Supplier;

import edu.wpi.first.wpilibj.geometry.Translation2d;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.FeedbackDevice;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.LimitSwitchPolarity;

public class MecanumModule {
    public static class Config {
        public boolean motorInverted;
        public boolean encoderInverted;
        public Supplier<FridolinsMotor> motorInitializer;
        public Translation2d mountingPoint;
    }

    public FridolinsMotor motor;

    public MecanumModule(Config config) {
        motor = config.motorInitializer.get();
        motor.setInverted(config.motorInverted);
        motor.setEncoderDirection(config.encoderInverted);
        motor.configEncoder(FeedbackDevice.QuadEncoder, 1);
        motor.enableForwardLimitSwitch(LimitSwitchPolarity.kNormallyClosed, false);
    }
}