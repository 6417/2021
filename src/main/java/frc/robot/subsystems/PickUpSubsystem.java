package frc.robot.subsystems;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Optional;

import com.revrobotics.CANSparkMax.IdleMode;

import edu.wpi.first.hal.util.UncleanStatusException;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.InstantCommand;
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
import ch.fridolins.server.Config;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.IdleModeType;

public class PickUpSubsystem extends PickUpBase {

    public static enum BallColor {
        blue((byte) 1), yellow((byte) 2), colorNotFound((byte) 0);

        public final byte byteRepresentation;

        private BallColor(byte byteRepresentation) {
            this.byteRepresentation = byteRepresentation;
        }
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
    private BallColor currentBallColor = BallColor.colorNotFound;

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
                .ifPresent((lightBarrier) -> DriverStation.getInstance().reportError(
                        "Lightbarrier might not be properly connected, expected false and accutaly was true (if lightbarrier was activate on purpose ignore this massage)",
                        false));

        blueBallColorVector = Vector3d.fromBallColorToVector(Constants.BallPickUp.blueBallColor);
        yellowBallColorVector = Vector3d.fromBallColorToVector(Constants.BallPickUp.yellowBallColor);
    }

    private void uiSocketServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            Socket clientSocket = serverSocket.accept();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            while (true) {
                
            }
        } catch (Exception e) {
            DriverStation.reportError("Error in ui server: " + e.getMessage(), false);
        }
    }

    private void initializeLightBarrier() {
        try {
            lightBarrier = Optional.of(new LightBarrier(0));
        } catch (UncleanStatusException e) {
            lightBarrier = Optional.empty();
            DriverStation.getInstance().reportError("LightBarrier not connected", false);
        }
    }

    @Override
    public void periodic() {
        super.periodic();
        if (lightBarrier.isEmpty()) {
            initializeLightBarrier();
        }
        getBallColor();
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
        if(currentColor.red > Constants.BallPickUp.comparativeValueRedLow && currentColor.blue < 60){
            return BallColor.yellow;
        }
        else if(currentColor.red < Constants.BallPickUp.comparativeValueBlueLow && currentColor.blue > Constants.BallPickUp.comparativeValueBlueHigh){
            return BallColor.blue;
        }
        return BallColor.colorNotFound;
    }

    @Override
    public void makeNewColorMeasurement() {
        Vector3d currentBallColorVector = new Vector3d(currentColor.red, currentColor.green, currentColor.blue);
        double currentVectorBlueVectorProduct = currentBallColorVector.dot(blueBallColorVector) / (blueBallColorVector.magnitude() * currentBallColorVector.magnitude());
        double currentVectorYellowVectorProduct = currentBallColorVector.dot(yellowBallColorVector) / (blueBallColorVector.magnitude() * currentBallColorVector.magnitude());

        if (currentVectorBlueVectorProduct < currentVectorYellowVectorProduct)
            currentBallColor = BallColor.yellow;
        else
            currentBallColor = BallColor.blue;
    }

    @Override
    public void resetBallColor() {
        currentBallColor = BallColor.colorNotFound;
    }

    @Override
    public void putColorInDashBoard() {
        SmartDashboard.putString("Ballcolor", getBallColor().toString());
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        lightBarrier
                .ifPresent((lightBarrier) -> builder.addBooleanProperty("LightBarrier", lightBarrier::isActiv, null));
        builder.addStringProperty("BallColor", () -> getBallColor().toString(), null);
        builder.addBooleanProperty("BallinTunnel", () -> ballInTunnel, null);

        Shuffleboard.getTab("Ball pick up").add("reset ball color", new InstantCommand(this::resetBallColor));
        Shuffleboard.getTab("Ball pick up").add("update ball color", new InstantCommand(this::makeNewColorMeasurement));
    }
}