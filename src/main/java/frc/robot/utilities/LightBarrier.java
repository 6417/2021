package frc.robot.utilities;

import edu.wpi.first.wpilibj.DigitalInput;

public class LightBarrier {

    private DigitalInput sensor;

    private boolean inverted;

    public LightBarrier(int DIOPort){
        sensor = new DigitalInput(DIOPort);        
    }

    public void setInverted(boolean inverted){
        this.inverted = inverted;
    }

    public boolean isActiv(){
        boolean output = sensor.get();
        if(inverted){
            return !output;
        } 
        else{
            return output;
        }
    } 
}