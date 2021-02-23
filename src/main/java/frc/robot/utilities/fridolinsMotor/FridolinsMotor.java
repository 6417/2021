package frc.robot.utilities.fridolinsMotor;

public interface FridolinsMotor {
    public enum LimitSwitchPolarity {
        kNormallyOpen, kNormallyClosed, kDisabled
    }

    public enum IdleModeType {
        kBrake, kCoast
    }

    public enum FridoFeedbackDevice {
        QuadEncoder, CANEncoder
    }

    public enum DirectionType {
        followMaster, invertMaster
    }
    
    public void set(double percent);

    public void setPosition(int position);

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

    public void configSelectedFeedbackSensor(FridoFeedbackDevice device, int countsPerRev);

    public void configOpenLoopRamp(double rate);
}