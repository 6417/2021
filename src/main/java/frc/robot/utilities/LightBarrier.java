package frc.robot.utilities;

import edu.wpi.first.wpilibj.DigitalInput;

public class LightBarrier {

    private static DigitalInput sensor;

    public LightBarrier(int DIOPort){
        sensor = new DigitalInput(DIOPort);        
    }

    public boolean isActiv(boolean inverted){
        boolean output = sensor.get();
        if(inverted){
            return !output;
        } 
        else{
            return output;
        }
    } 
}