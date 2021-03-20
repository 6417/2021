package frc.robot.utilities.fridolinsMotor;

import edu.wpi.first.wpilibj.SpeedController;
import frc.robot.utilities.PIDValues;

public interface FridolinsMotor extends SpeedController{
    public enum LimitSwitchPolarity {
        kNormallyOpen, kNormallyClosed, kDisabled
    }

    public enum IdleModeType {
        kBrake, kCoast
    }

    public enum FeedbackDevice {
        QuadEncoder, BuiltIn
    }

    public enum DirectionType {
        followMaster, invertMaster
    }

    public static final boolean debugMode = false;

    public void set(double velocity);
    
    public void setPosition(double position);

    public void setVelocity(double velocity);

    public void enableForwardLimitSwitch(LimitSwitchPolarity polarity, boolean enable);

    public void enableReverseLimitSwitch(LimitSwitchPolarity polarity, boolean enable);

    public boolean isForwardLimitSwitchActive();

    public boolean isReverseLimitSwitchActive();

    public void setIdleMode(IdleModeType type);

    public void follow(FridolinsMotor master, DirectionType direction);

    public void setDirection(boolean forward);

    public void setEncoderDirection(boolean inverted);

    public void setEncoderPosition(double position);

    public double getEncoderTicks();

    public double getEncoderSpeed();

    public void factoryDefault();

    public void configEncoder(FeedbackDevice device, int countsPerRev);

    public void configOpenLoopRamp(double rate);

    public void setPID(PIDValues pidValues);

    public void putDataInCSVFile(String filePath);
}