package frc.robot.utilities.fridolinsMotor;

import frc.robot.utilities.PIDValues;

public interface FridolinsMotor {
    public enum LimitSwitchPolarity {
        kNormallyOpen, kNormallyClosed, kDisabled
    }

    public enum IdleModeType {
        kBrake, kCoast
    }

    public enum FeedbackDevice {
        QuadEncoder, CANEncoder, HallSensor
    }

    public enum DirectionType {
        followMaster, invertMaster
    }
    
    public void set(double percent);

    public void setPosition(double position);

    public void enableForwardLimitSwitch(LimitSwitchPolarity polarity, boolean enable);

    public void enableReverseLimitSwitch(LimitSwitchPolarity polarity, boolean enable);

    public boolean isForwardLimitSwitchActive();

    public boolean isReverseLimitSwitchActive();

    public void setIdleMode(IdleModeType type);

    public void follow(FridolinsMotor master, DirectionType direction);

    public void setDirection(boolean forward);

    public void setSensorDirection(boolean inverted);

    public void setSensorPosition(double position);

    public double getEncoderTicks();

    public void factoryDefault();

    public void configSelectedFeedbackSensor(FeedbackDevice device, int countsPerRev);

    public void configOpenLoopRamp(double rate);

    public void setPID(PIDValues pidValues);

    public void setVelocity(double velocity);
}