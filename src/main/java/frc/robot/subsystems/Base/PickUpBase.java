package frc.robot.subsystems.base;

import java.util.Optional;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.PickUpSubsystem.BallColor;
import frc.robot.utilities.GroveColorSensor.Color;


public class PickUpBase extends SubsystemBase{

    public PickUpBase(){
    }

    public void stopMotors(){
    }

    public void test(){
    }

    public void pickUpBall(){
    }

    public void pickUpBallLatchedBoolean(){
    }

    public void loadBall(){
    }

    public void releaseBall(){
    }

    public Optional<Boolean> getLightBarrier(){
        return Optional.empty();
    }

    public BallColor getBallColor(){
        return null;
    }

    public Color getRGB(){
        return null;
    }

    public void putColorInDashBoard(){
    }
}