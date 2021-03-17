package frc.robot.utilities.fridolinsMotor;

import frc.robot.utilities.CSVLogger;
import frc.robot.utilities.PIDValues;

public class FridoMotorLogger implements FridolinsMotor {
    private static CSVLogger logger;
    private static boolean isLoggerActiv;

    public FridoMotorLogger(int deviceNumber) {
        System.out.println("created Motor at device ID:" + deviceNumber);
       isLoggerActiv = false;
    }

    public FridoMotorLogger(int deviceNumber, String filePath) {
        System.out.println("created Motor at device ID:" + deviceNumber);
        logger = new CSVLogger(filePath);
        isLoggerActiv = true;
    }

    public void set(double speed){
        if(isLoggerActiv){
            logger.put("Speed", speed);
        }
        else{
            System.out.println(speed);
        }
    }

    @Override
    public void setPosition(double position) {
        if(isLoggerActiv){
            logger.put("Sollwert Position", position);
        }        
        else
            System.out.println(position);
    }

    @Override
    public void setVelocity(double velocity) {
        if(isLoggerActiv){
            logger.put("Sollwert Geschwindigkeit", velocity);
        }
        else
            System.out.println(velocity);
    }

    @Override
    public void enableForwardLimitSwitch(LimitSwitchPolarity polarity, boolean enable) {
    }

    @Override
    public void enableReverseLimitSwitch(LimitSwitchPolarity polarity, boolean enable) {
    }

    @Override
    public boolean isForwardLimitSwitchActive() {
        return true;
    }

    @Override
    public boolean isReverseLimitSwitchActive() {
        return true;
    }

    @Override
    public void setIdleMode(IdleModeType type) {
    }

    @Override
    public void follow(FridolinsMotor master, DirectionType direction) {
        if(isLoggerActiv){
            logger.put("Master", master.toString());
        }
        else
            System.out.println(master.toString());
    }

    @Override
    public void setDirection(boolean forward) {
    }

    @Override
    public void setEncoderDirection(boolean inverted) {
    }

    @Override
    public void setEncoderPosition(double position) {
    }

    @Override
    public double getEncoderTicks() {
        return 0;
    }

    @Override
    public void factoryDefault() {
    }

    @Override
    public void configOpenLoopRamp(double rate) {
    }


    @Override
    public void configEncoder(FeedbackDevice device, int countsPerRev) {
    }

    @Override
    public void setPID(PIDValues pidValues) {
        if(isLoggerActiv){
            logger.put("P", pidValues.kP);
            logger.put("I", pidValues.kI);
            logger.put("D", pidValues.kD);
            if(pidValues.kF.isPresent()){
                logger.put("F", pidValues.kF.get());
            }
        }
        else{
            System.out.println("P: " + pidValues.kP + " I: " + pidValues.kI + " D: " + pidValues.kD);
        }
    }

    @Override
    public void putDataInCSVFile(String filePath){ // writes encoderPosition, speed, PID velocity (Sollwert), PID position (Sollwert)... to a csv file
        logger.open();
        if(FridolinsMotor.debugMode){
            logger = new CSVLogger(filePath);
            logger.put("EncoderTicks", this.getEncoderTicks());
            logger.put("Speed", 0);
            logger.put("Sollwert velocity", 0);
            logger.put("Sollwert position", 0);
            logger.put("PID P", 0);
            logger.put("PID I", 0);
            logger.put("PID D", 0);  
            logger.put("PID F", 0);

            logger.writeToFile();
            logger.close();       
        }
    }

    @Override
    public double get() {
        return 0;
    }

    @Override
    public void setInverted(boolean isInverted) {

    }

    @Override
    public boolean getInverted() {
        return false;
    }

    @Override
    public void disable() {

    }

    @Override
    public void stopMotor() {

    }

    @Override
    public void pidWrite(double output) {

    }
} 