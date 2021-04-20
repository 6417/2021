package frc.robot.subsystems;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants;
import frc.robot.subsystems.base.PickUpBase;
import frc.robot.utilities.GroveColorSensor;
import frc.robot.utilities.LightBarrier;
import frc.robot.utilities.GroveColorSensor.Color;
import frc.robot.utilities.GroveColorSensorI2C.Gain;
import frc.robot.utilities.GroveColorSensorI2C.IntegrationTime;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;
import frc.robot.utilities.Timer;
import frc.robot.utilities.LatchedBoolean;

public class PickUpSubsystem extends PickUpBase {

    public static enum BallColor {
        blue, yellow, colorNotFound
    }

    private static PickUpBase instance;

    private GroveColorSensor.Color currentColor;

    private FridolinsMotor pickUpMotor;
    private FridolinsMotor tunnelMotor;

    private GroveColorSensor colorSensor;

    // private NetworkTableEntry colorBox;

    private boolean isBallintunnel;

    private Thread updateBallColorThread; // we use this thread to update the ballcolor (there msut be a delay)

    private LightBarrier lightBarrier;
    
    private LatchedBoolean latchedBoolean;

    public PickUpSubsystem() {
        pickUpMotor = Constants.BallPickUp.pickUpMotor.get();
        tunnelMotor = Constants.BallPickUp.tunnelMotor.get();
        // tunnelMotor.setInverted(Constants.BallPickUp.tunnelMotorInvertation);

        // factory defaults
        pickUpMotor.factoryDefault();
        tunnelMotor.factoryDefault();

        // Encoders
        pickUpMotor.configEncoder(FridolinsMotor.FeedbackDevice.CANEncoder,
                Constants.BallPickUp.countsPerRevPickUpMotor);
        tunnelMotor.configEncoder(FridolinsMotor.FeedbackDevice.CANEncoder,
                Constants.BallPickUp.countsPerRevTunnelMotor);

        // light barriers
        lightBarrier = new LightBarrier(0);
        lightBarrier.setInverted(Constants.BallPickUp.isLightBarrierInverted);

        // color sensor and colorthread to update Ballcolor
        colorSensor = new GroveColorSensor(Port.kMXP, IntegrationTime._50MS, Gain.X1);

        updateBallColorThread = new Thread(this::updateBallColorLoop);
        updateBallColorThread.start();

        latchedBoolean = new LatchedBoolean(false ,LatchedBoolean.EdgeDetection.RISING);
    }
   
    public static PickUpBase getInstance() {
        if (instance == null) {
            if (Constants.BallPickUp.isEnabled) {
                instance = new PickUpSubsystem();
                // instance.setDefaultCommand();
            } else {
                instance = new PickUpBase();
            }
        }
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

    public boolean getLightBarrier() {
        return lightBarrier.isActiv();
    }

    @Override
    public void pickUpBall() {
        pickUpMotor.set(Constants.BallPickUp.pickUpSpeed);
        tunnelMotor.set(-Constants.BallPickUp.tunnelMotorPickUpSpeed);
    }

    @Override
    public void loadBall() {
    }

    @Override
    public void releaseBall() {
        pickUpMotor.set(-Constants.BallPickUp.releaseSpeed);
        tunnelMotor.set(Constants.BallPickUp.releaseSpeed);
    }

    @Override
    public void stopMotors() {
        pickUpMotor.stopMotor();
        tunnelMotor.stopMotor();
    }

    @Override
    public Color getRGB() {
        return currentColor;
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

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.addBooleanProperty("LightBarrier", lightBarrier::isActiv, null);
    }
}