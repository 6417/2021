package frc.robot.utilities.fridolinsMotor;

import edu.wpi.first.wpilibj.Talon;
import frc.robot.utilities.PIDValues;

public class FridoTalon extends Talon implements FridolinsMotor {

    public FridoTalon(int channel) {
        super(channel);
    }

    @Override
    public void setVelocity(double velocity) {
        throw new Error("Not implemented");
    }

    @Override
    public void enableForwardLimitSwitch(LimitSwitchPolarity polarity, boolean enable) {
        throw new Error("Not implemented");

    }

    @Override
    public void enableReverseLimitSwitch(LimitSwitchPolarity polarity, boolean enable) {
        throw new Error("Not implemented");

    }

    @Override
    public boolean isForwardLimitSwitchActive() {
        throw new Error("Not implemented");
    }

    @Override
    public boolean isReverseLimitSwitchActive() {
        throw new Error("Not implemented");
    }

    @Override
    public void setIdleMode(IdleModeType type) {
        throw new Error("Not implemented");

    }

    @Override
    public void follow(FridolinsMotor master, DirectionType direction) {
        throw new Error("Not implemented");

    }

    @Override
    public void setDirection(boolean forward) {
        throw new Error("Not implemented");

    }

    @Override
    public void setEncoderDirection(boolean inverted) {
        throw new Error("Not implemented");

    }

    @Override
    public void setEncoderPosition(double position) {
        throw new Error("Not implemented");

    }

    @Override
    public double getEncoderTicks() {
        throw new Error("Not implemented");
    
    }

    @Override
    public void factoryDefault() {
        throw new Error("Not implemented");

    }

    @Override
    public void configEncoder(FeedbackDevice device, int countsPerRev) {
        throw new Error("Not implemented");

    }

    @Override
    public void configOpenLoopRamp(double rate) {
        throw new Error("Not implemented");

    }

    @Override
    public void setPID(PIDValues pidValues) {
        throw new Error("Not implemented");

    }

    @Override
    public void putDataInCSVFile(String filePath) {
        throw new Error("Not implemented");

    }

}