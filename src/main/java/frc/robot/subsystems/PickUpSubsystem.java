package frc.robot.subsystems;

import java.util.Map;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants;
import frc.robot.subsystems.Base.PickUpBase;
import frc.robot.utilities.GroveColorSensor;
import frc.robot.utilities.GroveColorSensorI2C.Gain;
import frc.robot.utilities.GroveColorSensorI2C.IntegrationTime;
import frc.robot.utilities.fridolinsMotor.FridoCANSparkMax;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;

public class PickUpSubsystem extends PickUpBase {

    public static enum BallColor {
        blue, yellow, colorNotFound
    }

    private static PickUpBase instance;

    private GroveColorSensor.Color currentColor;

    private FridolinsMotor pickUpMotor;
    private FridolinsMotor tunnelMotor;

    private GroveColorSensor colorSensor;

    private NetworkTableEntry colorBox;

    private  Thread updateBallColorThread; // we use this thread to update the ballcolor (there msut be a delay)

    public PickUpSubsystem() {
        pickUpMotor = Constants.BallPickUp.pickUpMotor.get();
        tunnelMotor = Constants.BallPickUp.tunnelMotor.get();

        pickUpMotor.factoryDefault();
        tunnelMotor.factoryDefault();

        colorSensor = new GroveColorSensor(Port.kOnboard, IntegrationTime._50MS, Gain.X1); // TODO stimmt Gain und
                                                                                       // Integrationtime??
        // colorBox = Shuffleboard.getTab("SmartDashboard").add("BallColor", false).withWidget(BuiltInWidgets.kBooleanBox)
        //         .withProperties(Map.of("Color when false", "#b3e6e6")).getEntry();
        
        
        updateBallColorThread = new Thread(this::updateBallColorLoop);
        updateBallColorThread.start();
    }

    public static PickUpBase getInstance() {
        if (instance == null && Constants.BallPickUp.isEnabled) {
            instance = new PickUpSubsystem();
        } else if(Constants.BallPickUp.isEnabled)
            instance = new PickUpBase();

        return instance;
    }

    private void updateBallColorLoop() {
        while (true) {
            currentColor = colorSensor.readRGB();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {}
        }
    }

    @Override
    public void pickUpBall(){

    }

    @Override
    public void transportBall(){
        
    }

    @Override
    public void setPickUpMotor() {
    }

    @Override
    public void stopPickUpMotor() {
        pickUpMotor.stopMotor();
    }

    @Override
    public void stopTunnelMotor() {
        tunnelMotor.stopMotor();
    }

    @Override
    public void stopMotors() {
        pickUpMotor.stopMotor();
        tunnelMotor.stopMotor();
    }

    public BallColor getBallColor() {
        //SmartDashboard.putString("RGB", getBallColor().toString());

        if(currentColor.blue < 50 && currentColor.red > 100){
            return BallColor.yellow;
        }
        else if (currentColor.blue > 70 && currentColor.red > 70){
            return BallColor.blue;
        }
        return BallColor.colorNotFound;     
    }

    public void test(){
        SmartDashboard.putString("color", getBallColor().toString());
    }

}
