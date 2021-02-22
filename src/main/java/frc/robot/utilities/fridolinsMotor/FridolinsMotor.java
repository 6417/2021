package frc.robot.utilities.fridolinsMotor;

public interface FridolinsMotor {
    public void set(double percent);

    public void setPosition(int position);

    public void enableForwardLimitSwitch(FridolinsLimitSwitchPolarity polarity, boolean enable);

    public void enableReverseLimitSwitch(FridolinsLimitSwitchPolarity polarity, boolean enable);

    public boolean isForwardLimitSwitchActive();

    public boolean isReverseLimitSwitchActive();

    public void setIdleMode(FridolinsIdleModeType type);

    public void follow(FridolinsMotor master, FridolinsDirectionType direction);

    public void setDirection(boolean forward);

    public void setSensorDirection(boolean inverted);

    public void setSensorPosition(double position);

    public double getEncoderTicks();

    public void factoryDefault();

    public void configSelectedFeedbackSensor(FridolinsFeedbackDevice device, int countsPerRev);

    public void configOpenLoopRamp(double rate);
}