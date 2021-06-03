package frc.robot.utilities;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DigitalInput;

public class LightBarrier {

    private AnalogInput sensor;

    private boolean inverted;

    public LightBarrier(int DIOPort){
        sensor = new AnalogInput(DIOPort);        
    }

    public void setInverted(boolean inverted){
        this.inverted = inverted;
    }

    public boolean isActiv(){
        boolean output = sensor.getValue() > 1800;
        System.out.println(sensor.getValue());
        if(inverted){
            return !output;
        } 
        else{
            return output;
        }
    } 
}