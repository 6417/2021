package frc.robot.utilities.fridolinsMotor;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Talon;
import frc.robot.utilities.PIDValues;

public class FridoTalon extends Talon implements FridolinsMotor {
    Encoder encoder;
    int encoderChannel1;
    int encoderChannel2;

    public FridoTalon(int channel, int encoderChannel1, int encoderChannel2) {
        super(channel);
        this.encoderChannel1 = encoderChannel1;
        this.encoderChannel2 = encoderChannel2;
    }

    @Override
    public void set(double speed) {
        super.set(speed);
    }

    @Override
    public void setPosition(double position) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setVelocity(double velocity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void enableForwardLimitSwitch(LimitSwitchPolarity polarity, boolean enable) {
        // TODO Auto-generated method stub

    }

    @Override
    public void enableReverseLimitSwitch(LimitSwitchPolarity polarity, boolean enable) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isForwardLimitSwitchActive() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isReverseLimitSwitchActive() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setIdleMode(IdleModeType type) {
        // TODO Auto-generated method stub

    }

    @Override
    public void follow(FridolinsMotor master, DirectionType direction) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setInverted(boolean forward) {
        super.setInverted(!forward);
    }

    @Override
    public void setEncoderDirection(boolean inverted) {
        if (this.encoder == null) {
            try {
                throw new Exception("Encoder not initialized");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.encoder.setReverseDirection(inverted);
    }

    @Override
    public void setEncoderPosition(double position) {
        if (this.encoder == null) {
            try {
                throw new Exception("Encoder not initialized");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (position != 0) {
            try {
                throw new Exception("DIO encoders can't set the position");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.encoder.reset();
    }

    @Override
    public double getEncoderTicks() {
        if (this.encoder == null) {
            try {
                throw new Exception("Encoder not initialized");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return encoder.get();
    }

    @Override
    public void factoryDefault() {
        // TODO Auto-generated method stub

    }

    @Override
    public void configEncoder(FeedbackDevice device, int countsPerRev) {
        this.encoder = new Encoder(encoderChannel1, encoderChannel2);
    }

    @Override
    public void configOpenLoopRamp(double rate) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPID(PIDValues pidValues) {
        // TODO Auto-generated method stub

    }

    @Override
    public void putDataInCSVFile(String filePath) {
        // TODO Auto-generated method stub

    }

    @Override
    public double getEncoderVelocity() {
        return this.encoder.getRate();
    }

    @Override
    public void selectPIDSlot(int slotIdx) {
        // TODO Auto-generated method stub

    }
}