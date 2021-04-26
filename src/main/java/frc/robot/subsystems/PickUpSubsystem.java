package frc.robot.subsystems;

import java.sql.Driver;
import java.util.Optional;

import edu.wpi.first.hal.util.UncleanStatusException;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Constants;
import frc.robot.commands.ballPickUp.PickUpDefaultCommand;
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

    public static boolean ballInTunnel;

    private Thread updateBallColorThread; // we use this thread to update the ballcolor (there msut be a delay)

    private Optional<LightBarrier> lightBarrier;

    public PickUpSubsystem() {
        pickUpMotor = Constants.BallPickUp.pickUpMotor.get();
        tunnelMotor = Constants.BallPickUp.tunnelMotor.get();
        pickUpMotor.factoryDefault();
        tunnelMotor.factoryDefault();

        // light barriers
        initializeLightBarrier();
        lightBarrier.ifPresent((lightBarrier) -> lightBarrier.setInverted(Constants.BallPickUp.isLightBarrierInverted));

        // color sensor and colorthread to updatje Ballcolor
        colorSensor = new GroveColorSensor(Port.kMXP, IntegrationTime._50MS, Gain.X1);

        updateBallColorThread = new Thread(this::updateBallColorLoop);
        updateBallColorThread.start();

        // defautl command
        CommandScheduler.getInstance().schedule(new PickUpDefaultCommand());

        lightBarrier.filter((lightBarrier) -> lightBarrier.isActiv())
            .ifPresent((lightBarrier) -> DriverStation.getInstance().reportError("Lightbarrier might not be properly connected, expected false and accutaly was true (if lightbarrier was activate on purpose ignore this massage)", false));
    }

    private void initializeLightBarrier() {
        try{
            lightBarrier = Optional.of(new LightBarrier(0));
        }catch(UncleanStatusException e)
        {
            lightBarrier = Optional.empty();
            DriverStation.getInstance().reportError("LightBarrier not connected", false);
        }
    }

    @Override
    public void periodic() {
        super.periodic();
        if(lightBarrier.isEmpty()){
            initializeLightBarrier();
        }
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

    @Override
    public Optional<Boolean> getLightBarrier() {
        return lightBarrier.flatMap((LightBarrier lightBarrier) -> Optional.of(lightBarrier.isActiv()));
    }

    @Override
    public void pickUpBall() {
        pickUpMotor.set(Constants.BallPickUp.pickUpSpeed);
        tunnelMotor.set(-Constants.BallPickUp.tunnelMotorPickUpSpeed);
    }

    @Override
    public void loadBall() {
        tunnelMotor.set(-0.5);
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
    public void putColorInDashBoard() {
        SmartDashboard.putString("Ballcolor", getBallColor().toString());
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        lightBarrier.ifPresent((lightBarrier) -> builder.addBooleanProperty("LightBarrier", lightBarrier::isActiv, null));
    }
}