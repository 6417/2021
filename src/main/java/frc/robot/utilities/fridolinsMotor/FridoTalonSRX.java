package frc.robot.utilities.fridolinsMotor;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

public class FridoTalonSRX extends WPI_TalonSRX implements FridolinsMotor{
    public FridoTalonSRX(int deviceID){
        super(deviceID);
    }

    @Override
    public void setPosition(int position) {
        super.set(ControlMode.Position, position);
    }

    private LimitSwitchNormal convertFromFridoLimitSwitchPolarity(LimitSwitchPolarity polarity) {
        switch(polarity){
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
        if(!enable) {
            polarity = LimitSwitchPolarity.kDisabled;
        }
        super.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, convertFromFridoLimitSwitchPolarity(polarity));
    }

    @Override
    public void enableReverseLimitSwitch(LimitSwitchPolarity polarity, boolean enable) {
        if(!enable) {
            polarity = LimitSwitchPolarity.kDisabled;
        }
        super.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, convertFromFridoLimitSwitchPolarity(polarity));
    }

    @Override
    public boolean isForwardLimitSwitchActive() {
        return getSensorCollection().isFwdLimitSwitchClosed();
    }

    @Override
    public boolean isReverseLimitSwitchActive() {
        return getSensorCollection().isRevLimitSwitchClosed();
    }

    private NeutralMode convertFromFridoIdleMode(IdleModeType type){
        switch(type){
            case kBrake:
                return NeutralMode.Brake;
            case kCoast:
                return NeutralMode.Coast;
            default:
                return NeutralMode.Brake;
        }
    }

    @Override
    public void setIdleMode(IdleModeType type) {
        super.setNeutralMode(convertFromFridoIdleMode(type));
    }

    @Override
    public void follow(FridolinsMotor master, DirectionType direction) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDirection(boolean forward) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSensorDirection(boolean inverted) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setSensorPosition(double position) {
        // TODO Auto-generated method stub

    }

    @Override
    public double getEncoderTicks() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void factoryDefault() {
        // TODO Auto-generated method stub

    }

    @Override
    public void configOpenLoopRamp(double rate) {
        // TODO Auto-generated method stub

    }

    @Override
    public void configSelectedFeedbackSensor(FeedbackDevice device, int countsPerRev) {
        // TODO Auto-generated method stub

    }

}