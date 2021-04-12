package frc.robot.subsystems;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants;
import frc.robot.subsystems.Base.PickUpBase;
import frc.robot.utilities.Controller;
import frc.robot.utilities.GroveColorSensor;
import frc.robot.utilities.LightBarrier;
import frc.robot.utilities.Controller.ControlJoystick;
import frc.robot.utilities.GroveColorSensor.Color;
import frc.robot.utilities.GroveColorSensorI2C.Gain;
import frc.robot.utilities.GroveColorSensorI2C.IntegrationTime;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.LimitSwitchPolarity;

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

    private boolean isBallintunnel;

    private Thread updateBallColorThread; // we use this thread to update the ballcolor (there msut be a delay)

    private LightBarrier lightBarrier;

    public PickUpSubsystem() {
        pickUpMotor = Constants.BallPickUp.pickUpMotor.get();
        tunnelMotor = Constants.BallPickUp.tunnelMotor.get();

        // Encoders
        pickUpMotor.configEncoder(FridolinsMotor.FeedbackDevice.QuadEncoder, Constants.BallPickUp.countsPerRevPickUpMotor);
        tunnelMotor.configEncoder(FridolinsMotor.FeedbackDevice.QuadEncoder, Constants.BallPickUp.countsPerRevTunnelMotor);

        // light barriers 
        lightBarrier = new LightBarrier(0);

        // factory defaults
        pickUpMotor.factoryDefault();
        tunnelMotor.factoryDefault();

        colorSensor = new GroveColorSensor(Port.kOnboard, IntegrationTime._50MS, Gain.X1); 

        // colorBox = Shuffleboard.getTab("SmartDashboard").add("BallColor",
        // false).withWidget(BuiltInWidgets.kBooleanBox)
        // .withProperties(Map.of("Color when false", "#b3e6e6")).getEntry();

        updateBallColorThread = new Thread(this::updateBallColorLoop);
        updateBallColorThread.start();
    }

    public static PickUpBase getInstance() {
        if (instance == null && Constants.BallPickUp.isEnabled) 
            instance = new PickUpSubsystem();

        else if (Constants.BallPickUp.isEnabled)
            instance = new PickUpBase();

        return instance;
    }

    private void updateBallColorLoop() {
        while (true) {
            currentColor = colorSensor.readRGB();
            SmartDashboard.putString("RGB", currentColor.toString());
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void pickUpBall() {
        SmartDashboard.putBoolean("BallInTunnel", lightBarrier.isActiv(Constants.BallPickUp.isLightBarrierInverted));
        if(!lightBarrier.isActiv(Constants.BallPickUp.isLightBarrierInverted)){
            pickUpMotor.set(Constants.BallPickUp.pickUpSpeed); 
            tunnelMotor.set(Constants.BallPickUp.pickUpSpeed); 
        }
        else{
            pickUpMotor.stopMotor();
            tunnelMotor.stopMotor();
            isBallintunnel = true;
        }
    }

    @Override
    public void loadBall() {
    }

    @Override
    public void stopMotors() {
        pickUpMotor.stopMotor();
        tunnelMotor.stopMotor();
    }

    @Override
    public Color getRGB(){
        return colorSensor.readRGB();
    }

    @Override
    public BallColor getBallColor() {
        if (currentColor.blue < Constants.BallPickUp.comparativeValueBlueLow
                && currentColor.red > Constants.BallPickUp.comparativeValueRedLow) { // wenn blau über 52 und rot über
                                                                                        // 100
            return BallColor.yellow;
        } else if (currentColor.blue > Constants.BallPickUp.comparativeValueBlueHigh
                && currentColor.red > Constants.BallPickUp.comparativeValueRedTwo) { // wenn blau über 65 und rot über
                                                                                        // 70
            return BallColor.blue;
        }
        return BallColor.colorNotFound;
    }
}