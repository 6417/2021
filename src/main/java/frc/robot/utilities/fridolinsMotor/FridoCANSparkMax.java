package frc.robot.utilities.fridolinsMotor;

import com.revrobotics.CANDigitalInput;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.EncoderType;

import frc.robot.utilities.CSVLogger;
import frc.robot.utilities.PIDValues;

public class FridoCANSparkMax extends CANSparkMax implements FridolinsMotor {

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
    
    CANDigitalInput forwardLimitSwitch;
    CANDigitalInput reverseLimitSwitch;
    CANPIDController pidController;
    CANEncoder encoder;
    int ticksPerRotation;

    public FridoCANSparkMax(int deviceID, com.revrobotics.CANSparkMaxLowLevel.MotorType motorType) {
        super(deviceID, motorType);
    }

    @Override
    public void set(double speed) {
        super.set(speed);
        this.speed = speed;
    }

    @Override
    public void setPosition(double position) {
        if (this.pidController != null) {
            this.pidController.setReference(position, ControlType.kPosition);
        } else {
            try {
                throw new Exception("PID Controller not initialized");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.position = position;
    }

    @Override
    public void setVelocity(double velocity) {
        if (this.pidController != null) {
            this.pidController.setReference(velocity, ControlType.kVelocity);
        } else {
            try {
                throw new Exception("PID Controller not initialized");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.velocity = velocity;
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
    public void setEncoderDirection(boolean inverted) {
        this.encoder.setInverted(inverted);
    }

    @Override
    public void setEncoderPosition(double position) {
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
            default:
                return EncoderType.kQuadrature;
        }
    }

    @Override
    public void configEncoder(FridolinsMotor.FeedbackDevice device, int countsPerRev) {
        this.encoder = super.getEncoder(convertFromFridoFeedbackDevice(device), countsPerRev);
        this.encoder.setPositionConversionFactor(countsPerRev);
    }

    public void selectBuiltinFeedbackSensor() {
        this.encoder = super.getEncoder();
        super.getEncoder().setPositionConversionFactor(0);
    }

    @Override
    public void configOpenLoopRamp(double rate) {
        super.setOpenLoopRampRate(rate);
    }

    @Override
    public void setPID(PIDValues pidValues) {
        this.pidController = super.getPIDController();
        this.pidController.setP(pidValues.kP);
        this.pidController.setI(pidValues.kI);
        this.pidController.setD(pidValues.kD);
        pidValues.kF.ifPresent((kF) -> this.pidController.setFF(kF));

        this.kP = pidValues.kP;
        this.kI = pidValues.kI;
        this.kD = pidValues.kD;
        if(pidValues.kF.isPresent()){
            this.kF = pidValues.kF.get();
            isKFEnabled = true;
        }
    }

    public void putDataInCSVFile(String filePath){ // writes encoderPosition, speed, PID velocity (Sollwert), PID position (Sollwert)... to a csv file
        logger = new CSVLogger(filePath);
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
    }
}