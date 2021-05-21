package frc.robot.subsystems.mecanum;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SlewRateLimiter;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.drive.RobotDriveBase;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveKinematics;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveOdometry;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveWheelSpeeds;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SendableRegistry;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.Constants;
import frc.robot.Controller;
import frc.robot.Robot;
import frc.robot.commands.mecanum.DefaultDriveCommand;
import frc.robot.commands.mecanum.FieldOriented;
import frc.robot.commands.mecanum.PickupOriented;
import frc.robot.commands.mecanum.SetSpeedFactor;
import frc.robot.commands.mecanum.SwitchDriveMode;
import frc.robot.commands.mecanum.ThrowerOriented;
import frc.robot.subsystems.Drive.DriveOrientation;
import frc.robot.subsystems.base.MecanumDriveSubsystemBase;
import frc.robot.utilities.Algorithms;

public class MecanumDriveSubsystem extends MecanumDriveSubsystemBase {

    private static MecanumDriveSubsystemBase instance;

    private SlewRateLimiter inputLimiterX;
    private SlewRateLimiter inputLimiterY;
    private SlewRateLimiter inputLimiterRotation;
    private DriveOrientation driveMode;
    private MecanumDrive drive;
    private MecanumDriveWheelSpeeds wheelSpeeds;
    private MecanumDriveKinematics mecanumDriveKinematics;
    private AHRS navx;
    private MecanumDriveOdometry odometry;
    private double speedFactor = Constants.MecanumDrive.defaultSpeedFac1or;
    private Map<Constants.Drive.MountingLocations, MecanumModule> modules;

    private MecanumDriveSubsystem() {
        inputLimiterX = new SlewRateLimiter(1.0 / Constants.MecanumDrive.SECONDS_TO_ACCELERATE);
        inputLimiterY = new SlewRateLimiter(1.0 / Constants.MecanumDrive.SECONDS_TO_ACCELERATE);
        inputLimiterRotation = new SlewRateLimiter(1.0 / Constants.MecanumDrive.SECONDS_TO_ACCELERATE);

        driveMode = DriveOrientation.ThrowerOriented;

        modules = Constants.MecanumDrive.moduleConfigs.entrySet().stream()
                .map(Algorithms.mapEntryFunction((config) -> new MecanumModule(config)))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        modules.entrySet().stream().forEach(
                (moduleEntry) -> Shuffleboard.getTab("Mecanum modules").add("Mecanum module " + moduleEntry.getKey().toString(), moduleEntry.getValue()));

        mecanumDriveKinematics = new MecanumDriveKinematics(
                Constants.MecanumDrive.moduleConfigs.get(Constants.Drive.MountingLocations.FrontLeft).mountingPoint,
                Constants.MecanumDrive.moduleConfigs.get(Constants.Drive.MountingLocations.BackLeft).mountingPoint,
                Constants.MecanumDrive.moduleConfigs.get(Constants.Drive.MountingLocations.FrontRight).mountingPoint,
                Constants.MecanumDrive.moduleConfigs.get(Constants.Drive.MountingLocations.BackRight).mountingPoint);
        wheelSpeeds = new MecanumDriveWheelSpeeds();

        drive = new MecanumDrive(modules.get(Constants.Drive.MountingLocations.FrontLeft),
                modules.get(Constants.Drive.MountingLocations.BackLeft),
                modules.get(Constants.Drive.MountingLocations.FrontRight),
                modules.get(Constants.Drive.MountingLocations.BackRight));

        drive.setSafetyEnabled(false);

        navx = Robot.getNavx();
        odometry = new MecanumDriveOdometry(mecanumDriveKinematics, navx.getRotation2d(),
                new Pose2d(0, 0, new Rotation2d(0)));
    }

    public void updateOutputFactorsOfModules() {
        MecanumModule moduleWithMostOutput = modules.values().stream()
                .max(Comparator.comparing((module) -> module.get())).get();
        if (moduleWithMostOutput.get() >= 1.0
                && moduleWithMostOutput.getAcceleration() > Constants.MecanumDrive.outputFactorAccerleratoinFreshHold) {
            double outputFactor = moduleWithMostOutput.getEncoderVelocity() / moduleWithMostOutput.getTargetVelocity();
            modules.values().stream().filter((module) -> module != moduleWithMostOutput)
                    .forEach(module -> module.updateOutputFactor(outputFactor));
        } else
            forEachModule((module) -> module.updateOutputFactor(1.0));
    }

    @Override
    public void periodic() {
        forEachModule((module) -> module.getAcceleration());
    }

    public static MecanumDriveSubsystemBase getInstance() {
        if (instance == null) {
            if (Constants.MecanumDrive.IS_ENABLED) {
                instance = new MecanumDriveSubsystem();
                instance.setDefaultCommand(new DefaultDriveCommand());
                // if (!Constants.SwerveDrive.enabled)
                // throw new Error("Swerve drive can't be enabled while swerve drive is
                // enabled");
            } else
                instance = new MecanumDriveSubsystemBase();
        }
        return instance;
    }

    public void forEachModule(Consumer<MecanumModule> function) {
        modules.values().stream().forEach(function);
    }

    private void resetEncoders() {
        forEachModule((module) -> module.resetEncoder());
    }

    private double convertEncoderSpeedToMetersPerSecond(double encoderSpeed) {
        return encoderSpeed / Constants.MecanumDrive.ticksPerRotation * Constants.MecanumDrive.wheelDiameter * Math.PI;
    }

    @Override
    public void updateOdometry() {
        this.wheelSpeeds.frontLeftMetersPerSecond = convertEncoderSpeedToMetersPerSecond(
                modules.get(Constants.Drive.MountingLocations.FrontLeft).getEncoderVelocity());
        this.wheelSpeeds.frontRightMetersPerSecond = convertEncoderSpeedToMetersPerSecond(
                modules.get(Constants.Drive.MountingLocations.FrontRight).getEncoderVelocity());
        this.wheelSpeeds.rearLeftMetersPerSecond = convertEncoderSpeedToMetersPerSecond(
                modules.get(Constants.Drive.MountingLocations.BackLeft).getEncoderVelocity());
        this.wheelSpeeds.rearRightMetersPerSecond = convertEncoderSpeedToMetersPerSecond(
                modules.get(Constants.Drive.MountingLocations.BackRight).getEncoderVelocity());
        odometry.update(navx.getRotation2d(), this.wheelSpeeds);
    }

    @Override
    public void resetOdometry() {
        navx.reset();
        resetEncoders();
        odometry.resetPosition(new Pose2d((double) 0, (double) 0, new Rotation2d(0)), navx.getRotation2d());
    }

    @Override
    public Pose2d getPosition() {
        return odometry.getPoseMeters();
    }

    @Override
    public void drive(double xSpeed, double ySpeed, double zRotation) {
        xSpeed *= speedFactor;
        ySpeed *= speedFactor;
        zRotation *= speedFactor;
        switch (driveMode) {
            case ThrowerOriented:
                drive.driveCartesian(inputLimiterX.calculate(xSpeed), inputLimiterY.calculate(-ySpeed),
                        inputLimiterRotation.calculate(zRotation));
                break;
            case PickUpOriented:
                drive.driveCartesian(inputLimiterX.calculate(xSpeed), inputLimiterY.calculate(-ySpeed),
                        inputLimiterRotation.calculate(zRotation), 180);
                break;
            case FieldOriented:
                drive.driveCartesian(inputLimiterX.calculate(xSpeed), inputLimiterY.calculate(-ySpeed),
                        inputLimiterRotation.calculate(zRotation), navx.getAngle());
                break;
        }
        this.updateOdometry();
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.addStringProperty("driveMode", () -> driveMode.toString(), null);
        super.initSendable(builder);
    }

    @Override
    public void configureButtonBindings(Joystick joystick) {
        JoystickButton slowSpeedModeButton = new JoystickButton(joystick,
                Constants.MecanumDrive.ButtonIds.slowSpeedMode);
        JoystickButton fieldOrientedButton = new JoystickButton(joystick,
                Constants.MecanumDrive.ButtonIds.fieledOriented);
        JoystickButton pickUpOrientedButton = new JoystickButton(joystick,
                Constants.MecanumDrive.ButtonIds.pickupOriented);
        JoystickButton throwerOrientedButton = new JoystickButton(joystick,
                Constants.MecanumDrive.ButtonIds.throwerOriented);
        JoystickButton switchDriveModeButton = new JoystickButton(joystick, Constants.MecanumDrive.ButtonIds.switchDriveMode);

        slowSpeedModeButton.whenPressed(Controller.runCommandAndCancelWhenPressedAgain(new SetSpeedFactor(Constants.MecanumDrive.slowModeSpeedFactor)));

        fieldOrientedButton.whenPressed(new FieldOriented());
        pickUpOrientedButton.whenPressed(new PickupOriented());
        throwerOrientedButton.whenPressed(new ThrowerOriented());
        switchDriveModeButton.whenPressed(new SwitchDriveMode());
    }

    @Override
    public void setDriveOrientation(DriveOrientation driveMode) {
        this.driveMode = driveMode;
    }

    @Override
    public void setSpeedFactor(double speedFactor) {
        this.speedFactor = speedFactor;
    }
}