package frc.robot.utilities.fridolinsMotor;

import java.util.Optional;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import frc.robot.utilities.CSVLogger;
import frc.robot.utilities.PIDValues;

public class FridoTalonSRX extends WPI_TalonSRX implements FridolinsMotor {
    // variables for CSVLogger:
    private CSVLogger logger;
    private double speed;
    private double position;
    private double velocity;
    private double kP;
    private double kI;
    private double kD;
    private double kF;
    private boolean isKFEnabled = false;

    public FridoTalonSRX(int deviceID) {

        super(deviceID);
        if (FridolinsMotor.debugMode)
            logger = new CSVLogger("/tmp/logFridoTalon_id_" + deviceID + ".csv");
    }

    @Override
    public void set(double speed){
        super.set(speed);
        this.speed = speed;
    }

    @Override
    public void setPosition(double position) {
        super.set(ControlMode.Position, position);
        this.position = position;
    }

    @Override
    public void setVelocity(double velocity) {
        super.set(ControlMode.Velocity, velocity);
        this.velocity = velocity;
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
    public void setInverted(boolean forward) {
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
            case BuiltIn:
                try {
                    throw new Exception("You cannot use Builtin Encoders with the TalonSRX Controllers");
                } catch (Exception e) {
                    e.printStackTrace();
                }

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
        if (pidValues.slotIdX.isPresent()) {
            super.config_kP(pidValues.slotIdX.get(), pidValues.kP);
            super.config_kI(pidValues.slotIdX.get(), pidValues.kI);
            super.config_kD(pidValues.slotIdX.get(), pidValues.kD);
            super.configPeakOutputForward(pidValues.peakOutputForward);
            super.configPeakOutputReverse(pidValues.peakOutputReverse);
            pidValues.cruiseVelocity.ifPresent((cruiseVelocity) -> super.configMotionCruiseVelocity((int) cruiseVelocity.doubleValue()));
            pidValues.acceleration.ifPresent((acceleration) -> super.configMotionAcceleration((int) acceleration.doubleValue()));
            pidValues.kF.ifPresent((kF) -> super.config_kF(pidValues.slotIdX.get(), kF));
            super.selectProfileSlot(pidValues.slotIdX.get(), 0);
        } else {
            try {
                throw new Exception("You have to give a slotID for TalonSRX pidControllers");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.kP = pidValues.kP;
        this.kI = pidValues.kI;
        this.kD = pidValues.kD;
        if(pidValues.kF.isPresent()) {
            this.kF = pidValues.kF.get();
            isKFEnabled = true;
        }
    }
    
    public void putDataInCSVFile(String filePath){ // writes encoderPosition, speed, PID velocity (Sollwert), PID position (Sollwert)... to a csv file
        logger.open();
        if(FridolinsMotor.debugMode){ 
            logger.put("EncoderTicks", this.getEncoderTicks());
            logger.put("Speed", speed);
            logger.put("setValue velocity", velocity);
            logger.put("setValue position", position);
            logger.put("PID P", kP);
            logger.put("PID I", kI);
            logger.put("PID D", kD);  
            if(isKFEnabled){
                logger.put("PID F", kF);
            }
            logger.writeToFile();
            logger.close(); 
        }     
    }

    @Override
    public double getEncoderVelocity() {
        return super.getSelectedSensorVelocity();
    }

    @Override
    public void selectPIDSlot(int slotIdx) {
        super.selectProfileSlot(slotIdx, 0);
    }
}