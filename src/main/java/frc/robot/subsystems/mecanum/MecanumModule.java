package frc.robot.subsystems.mecanum;

import java.util.Optional;
import java.util.function.Supplier;

import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import frc.robot.utilities.PIDValues;
import frc.robot.utilities.Timer;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.FeedbackDevice;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.LimitSwitchPolarity;

public class MecanumModule implements SpeedController, Sendable {
    public static class Config implements Cloneable {
        public boolean motorInverted;
        public boolean encoderInverted;
        public Supplier<FridolinsMotor> motorInitializer;
        public Translation2d mountingPoint;
        public double maxSpeed;
        public PIDValues pidValues;

        public Config clone() {
            Config copy = new Config();
            copy.motorInverted = motorInverted;
            copy.encoderInverted = encoderInverted;
            if (motorInitializer != null)
                copy.motorInitializer = motorInitializer::get;
            else
                copy.motorInitializer = null;
            copy.mountingPoint = mountingPoint;
            copy.maxSpeed = maxSpeed;
            if (pidValues != null)
                copy.pidValues = pidValues.clone();
            else
                copy.pidValues = null;
            return copy;
        }
    }

    public FridolinsMotor motor;
    public double maxSpeed;
    public double targetVelocity = 0;
    private double outputFactor = 1;
    private double previousVelocity = 0;
    private Timer accelerationTimer = new Timer(System::currentTimeMillis);

    public MecanumModule(Config config) {
        motor = config.motorInitializer.get();
        setInverted(config.motorInverted);
        motor.configEncoder(FeedbackDevice.QuadEncoder, 1);
        motor.setEncoderDirection(config.encoderInverted);
        motor.enableForwardLimitSwitch(LimitSwitchPolarity.kNormallyClosed, false);
        motor.setPID(config.pidValues);
        maxSpeed = config.maxSpeed;
    }

    public void resetEncoder() {
        motor.setEncoderPosition(0);
    }

    public double getEncoderVelocity() {
        return motor.getEncoderVelocity();
    }

    @Override
    public void pidWrite(double output) {
        motor.set(output);
    }

    public void updateOutputFactor(double factor) {
        outputFactor = factor;
    }

    @Override
    public void set(double speed) {
        // TODO: Implement this method that it uses a velocity pid and corrects the
        // inconsistency of the max velocity
        motor.setVelocity(speed * maxSpeed * outputFactor);
        targetVelocity = speed * maxSpeed * outputFactor;
    }

    @Override
    public double get() {
        return motor.get();
    }

    /**
     * This function should be called every iteration of the robot periodic when
     * {@link #getAcceleration()} is used to get better results.
     */
    public void updateAcceleration() {
        previousVelocity = getEncoderVelocity();
        accelerationTimer.start();
    }

    /**
     * <b>The {@link #updateAcceleration()} function should be called every
     * iteration of the robot periodic, to get better results.</b>
     * 
     * @return the current acceleration of the module in encoder velocity units per
     *         miliseconds. If its called the first time it will retrun the current
     *         encoder velocity.
     */
    public double getAcceleration() {
        double result = accelerationTimer.getPastTimeAndRestart()
                .flatMap((time) -> Optional.of((getEncoderVelocity() - previousVelocity) / time))
                .orElse(getEncoderVelocity());
        previousVelocity = getEncoderVelocity();
        return result;
    }

    public double getTargetVelocity() {
        return targetVelocity;
    }

    @Override
    public void setInverted(boolean isInverted) {
        motor.setInverted(isInverted);
    }

    @Override
    public boolean getInverted() {
        return motor.getInverted();
    }

    @Override
    public void disable() {
        motor.disable();
    }

    @Override
    public void stopMotor() {
        motor.stopMotor();
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.addDoubleProperty("Encoder Position", motor::getEncoderTicks, null);
        builder.addDoubleProperty("Encoder Velocity", motor::getEncoderVelocity, null);
        builder.addDoubleProperty("Desired Velocity", () -> targetVelocity, null);
        builder.addDoubleProperty("output factor", () -> outputFactor, null);
    }
}