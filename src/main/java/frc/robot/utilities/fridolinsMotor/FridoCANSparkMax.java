package frc.robot.utilities.fridolinsMotor;

import com.revrobotics.CANDigitalInput;
import com.revrobotics.CANDigitalInput.LimitSwitchPolarity;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.EncoderType;

public class FridoCANSparkMax extends CANSparkMax implements FridolinsMotor {
    CANDigitalInput forwardLimitSwitch;
    CANDigitalInput reverseLimitSwitch;
    CANEncoder encoder;

    public FridoCANSparkMax(int deviceID, com.revrobotics.CANSparkMaxLowLevel.MotorType motorType) {
        super(deviceID, motorType);
    }

    @Override
    public void setPosition(int position) {
        // TODO Auto-generated method stub
    }

    private CANDigitalInput.LimitSwitchPolarity convertFromFridoLimitSwitchPolarity(FridolinsMotor.LimitSwitchPolarity polarity) {
        switch (polarity) {
            case kNormallyOpen:
                return CANDigitalInput.LimitSwitchPolarity.kNormallyOpen;
            case kNormallyClosed:
                return CANDigitalInput.LimitSwitchPolarity.kNormallyClosed;
            default:
                return CANDigitalInput.LimitSwitchPolarity.kNormallyOpen;
        }
    }

    private CANDigitalInput getForwardLimitSwitchInstance() {
        return forwardLimitSwitch;
    }

    private CANDigitalInput getForwardLimitSwitchInstance(FridolinsMotor.LimitSwitchPolarity polarity) {
        if (forwardLimitSwitch == null) {
            forwardLimitSwitch = super.getForwardLimitSwitch(convertFromFridoLimitSwitchPolarity(polarity));
        }
        return forwardLimitSwitch;
    }

    private CANDigitalInput getReverseLimitSwitchInstance() {
        return reverseLimitSwitch;
    }

    private CANDigitalInput getReverselimitSwitchInstance(FridolinsMotor.LimitSwitchPolarity polarity) {
        if (reverseLimitSwitch == null) {
            reverseLimitSwitch = super.getReverseLimitSwitch(convertFromFridoLimitSwitchPolarity(polarity));
        }
        return reverseLimitSwitch;
    }

    @Override
    public void enableForwardLimitSwitch(FridolinsMotor.LimitSwitchPolarity polarity, boolean enable) {
        getForwardLimitSwitchInstance(polarity).enableLimitSwitch(enable);
    }

    @Override
    public void enableReverseLimitSwitch(FridolinsMotor.LimitSwitchPolarity polarity, boolean enable) {
        getReverselimitSwitchInstance(polarity).enableLimitSwitch(enable);
    }

    @Override
    public boolean isForwardLimitSwitchActive() {
        return getForwardLimitSwitchInstance().get();
    }

    @Override
    public boolean isReverseLimitSwitchActive() {
        return getReverseLimitSwitchInstance().get();
    }

    private IdleMode convertFromFridoIdleModeType(FridolinsMotor.IdleModeType idleModeType) {
        switch (idleModeType) {
            case kBrake:
                return IdleMode.kBrake;
            case kCoast:
                return IdleMode.kCoast;
            default:
                return IdleMode.kBrake;
        }
    }

    @Override
    public void setIdleMode(FridolinsMotor.IdleModeType type) {
        this.setIdleMode(convertFromFridoIdleModeType(type));
    }

    private boolean convertFromFridoDirectionsType(FridolinsMotor.DirectionType direction) {
        switch (direction) {
            case followMaster:
                return false;
            case invertMaster:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void follow(FridolinsMotor master, FridolinsMotor.DirectionType direction) {
        if (master instanceof FridoCANSparkMax) {
            super.follow((FridoCANSparkMax) master, convertFromFridoDirectionsType(direction));
        }
    }

    @Override
    public void setDirection(boolean forward) {
        this.setDirection(forward);
    }

    @Override
    public void setSensorDirection(boolean inverted) {
        this.encoder.setInverted(inverted);
    }

    @Override
    public void setSensorPosition(double position) {
        this.encoder.setPosition(position);
    }

    @Override
    public double getEncoderTicks() {
        return this.encoder.getPosition();
    }

    @Override
    public void factoryDefault() {
        super.restoreFactoryDefaults();
    }

    private EncoderType convertFromFridoFeedbackDevice(FridolinsMotor.FeedbackDevice device) {
        switch (device) {
            case QuadEncoder:
                return EncoderType.kQuadrature;
            case HallSensor:
                return EncoderType.kHallSensor;
            default:
                return EncoderType.kQuadrature;
        }
    }

    @Override
    public void configSelectedFeedbackSensor(FridolinsMotor.FeedbackDevice device, int countsPerRev) {
        this.encoder = super.getEncoder(convertFromFridoFeedbackDevice(device), countsPerRev);
    }

    public void selectBuiltinFeedbackSensor() {
        this.encoder = super.getEncoder();
    }

    @Override
    public void configOpenLoopRamp(double rate) {
        super.setOpenLoopRampRate(rate);
    }
}