package frc.robot.subsystems;

import java.util.Optional;

import com.revrobotics.CANSparkMax.IdleMode;

import edu.wpi.first.hal.util.UncleanStatusException;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Constants;
import frc.robot.commands.ballPickUp.PickUpDefaultCommand;
import frc.robot.subsystems.base.PickUpBase;
import frc.robot.utilities.GroveColorSensor;
import frc.robot.utilities.LightBarrier;
import frc.robot.utilities.Vector3d;
import frc.robot.utilities.GroveColorSensor.Color;
import frc.robot.utilities.GroveColorSensorI2C.Gain;
import frc.robot.utilities.GroveColorSensorI2C.IntegrationTime;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.IdleModeType;

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

    private final Vector3d blueBallColorVector;
    private final Vector3d yellowBallColorVector;
    private final Vector3d clearColorVector;

    public PickUpSubsystem() {
        pickUpMotor = Constants.BallPickUp.pickUpMotor.get();
        tunnelMotor = Constants.BallPickUp.tunnelMotor.get();
        pickUpMotor.setIdleMode(IdleModeType.kCoast);
        pickUpMotor.factoryDefault();
        tunnelMotor.factoryDefault();

        // light barriers
        initializeLightBarrier();
        lightBarrier.ifPresent((lightBarrier) -> lightBarrier.setInverted(Constants.BallPickUp.isLightBarrierInverted));

        // color sensor and colorthread to updatje Ballcolor
        colorSensor = new GroveColorSensor(Port.kMXP, IntegrationTime._50MS, Gain.X1);

        updateBallColorThread = new Thread(this::updateBallColorLoop);
        updateBallColorThread.start();

        // defaultcommand
        CommandScheduler.getInstance().schedule(new PickUpDefaultCommand());

        lightBarrier.filter((lightBarrier) -> lightBarrier.isActiv())
            .ifPresent((lightBarrier) -> DriverStation.getInstance().reportError("Lightbarrier might not be properly connected, expected false and accutaly was true (if lightbarrier was activate on purpose ignore this massage)", false));

        blueBallColorVector = Vector3d.fromBallColorToVector(Constants.BallPickUp.blueBallColor);
        yellowBallColorVector = Vector3d.fromBallColorToVector(Constants.BallPickUp.yellowBallColor);
        clearColorVector = Vector3d.fromBallColorToVector(Constants.BallPickUp.clearColor);
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
                // instance.setDefaultCommand(new PickUpDefaultCommand());
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
        Vector3d currentBallColorVector = new Vector3d(currentColor.red, currentColor.green, currentColor.blue);

        System.out.println(currentBallColorVector.normalize().dot(clearColorVector.normalize()));

        if(currentBallColorVector.normalize().dot(clearColorVector.normalize()) < Constants.BallPickUp.comparativeValueClear) {
            double currentVectorBlueVectorProduct = currentBallColorVector.normalize().dot(blueBallColorVector.normalize());
            double currentVectorYellowVectorProduct = currentBallColorVector.normalize().dot(yellowBallColorVector.normalize());

            if(currentVectorBlueVectorProduct < currentVectorYellowVectorProduct)
                return BallColor.yellow;
            else
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
        builder.addStringProperty("BallColor", getBallColor()::toString, null);
        builder.addBooleanProperty("BallinTunnel", () -> ballInTunnel, null);
    }
}