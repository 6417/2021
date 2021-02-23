package frc.robot.utilities.fridolinsMotor;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import frc.robot.utilities.PIDValues;

public class FridoTalonSRX extends WPI_TalonSRX implements FridolinsMotor {
    public FridoTalonSRX(int deviceID) {
        super(deviceID);
    }

    @Override
    public void setPosition(int position) {
        super.set(ControlMode.Position, position);
    }

    private LimitSwitchNormal convertFromFridoLimitSwitchPolarity(LimitSwitchPolarity polarity) {
        switch (polarity) {
            case kNormallyOpen:
                return LimitSwitchNormal.NormallyOpen;
            case kNormallyClosed:
                return LimitSwitchNormal.NormallyClosed;
            default:
                return LimitSwitchNormal.Disabled;
        }
    }

    @Override
    public void enableForwardLimitSwitch(LimitSwitchPolarity polarity, boolean enable) {
        if (!enable) {
            polarity = LimitSwitchPolarity.kDisabled;
        }
        super.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector,
                convertFromFridoLimitSwitchPolarity(polarity));
    }

    @Override
    public void enableReverseLimitSwitch(LimitSwitchPolarity polarity, boolean enable) {
        if (!enable) {
            polarity = LimitSwitchPolarity.kDisabled;
        }
        super.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector,
                convertFromFridoLimitSwitchPolarity(polarity));
    }

    @Override
    public boolean isForwardLimitSwitchActive() {
        return getSensorCollection().isFwdLimitSwitchClosed();
    }

    @Override
    public boolean isReverseLimitSwitchActive() {
        return getSensorCollection().isRevLimitSwitchClosed();
    }

    private NeutralMode convertFromFridoIdleMode(IdleModeType type) {
        switch (type) {
            case kBrake:
                return NeutralMode.Brake;
            case kCoast:
                return NeutralMode.Coast;
            default:
                return NeutralMode.Brake;
        }
    }

    private InvertType convertFromFridoDirectionsType(FridolinsMotor.DirectionType direction) {
        switch (direction) {
            case followMaster:
                return InvertType.FollowMaster;
            case invertMaster:
                return InvertType.OpposeMaster;
            default:
                return InvertType.FollowMaster;
        }
    }

    @Override
    public void setIdleMode(IdleModeType type) {
        super.setNeutralMode(convertFromFridoIdleMode(type));
    }

    @Override
    public void follow(FridolinsMotor master, DirectionType direction) {
        if (master instanceof FridoTalonSRX) {
            super.follow((FridoTalonSRX) master);
            super.setInverted(convertFromFridoDirectionsType(direction));
        }
    }

    @Override
    public void setDirection(boolean forward) {
        super.setInverted(forward);
    }

    @Override
    public void setEncoderDirection(boolean inverted) {
        super.setSensorPhase(inverted);
    }

    @Override
    public void setEncoderPosition(double position) {
        super.setSelectedSensorPosition((int) position);
    }

    @Override
    public double getEncoderTicks() {
        return super.getSelectedSensorPosition();
    }

    @Override
    public void factoryDefault() {
        super.configFactoryDefault();
    }

    @Override
    public void configOpenLoopRamp(double rate) {
        super.configOpenloopRamp(rate);

    }

    private com.ctre.phoenix.motorcontrol.FeedbackDevice convertFromFridoFeedbackDevice(
            FridolinsMotor.FeedbackDevice device) {
        switch (device) {
            case QuadEncoder:
                return com.ctre.phoenix.motorcontrol.FeedbackDevice.QuadEncoder;

            default:
                return com.ctre.phoenix.motorcontrol.FeedbackDevice.QuadEncoder;
        }
    }

    @Override
    public void configEncoder(FeedbackDevice device, int countsPerRev) {
        super.configSelectedFeedbackSensor(convertFromFridoFeedbackDevice(device));
    }

    @Override
    public void setPID(PIDValues pidValues) {
        // TODO Auto-generated method stub

    }

    
}