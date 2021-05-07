package frc.robot.subsystems.mecanum;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SlewRateLimiter;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveKinematics;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveOdometry;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveWheelSpeeds;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.Constants;
import frc.robot.Robot;
import frc.robot.commands.DefaultDriveCommand;
import frc.robot.commands.mecanum.FieldOriented;
import frc.robot.commands.mecanum.PickupOriented;
import frc.robot.commands.mecanum.ThrowerOriented;
import frc.robot.subsystems.Drive.DriveMode;
import frc.robot.subsystems.base.MecanumDriveSubsystemBase;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.FeedbackDevice;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.LimitSwitchPolarity;

public class MecanumDriveSubsystem extends MecanumDriveSubsystemBase {

    
    private static MecanumDriveSubsystemBase instance;

    private SlewRateLimiter inputLimiterX;
    private SlewRateLimiter inputLimiterY;
    private SlewRateLimiter inputLimiterRotation;

    private DriveMode driveMode;

    private FridolinsMotor frontrightMotor;
    private FridolinsMotor backrightMotor;
    private FridolinsMotor frontleftMotor;
    private FridolinsMotor backleftMotor;

    private MecanumDrive drive;
    private MecanumDriveWheelSpeeds wheelSpeeds;
    private MecanumDriveKinematics mecanumDriveKinematics;

    private AHRS navx;

    private MecanumDriveOdometry odometry;

    private double speedFactor = Constants.MecanumDrive.defaultSpeedFac1or;

    private MecanumDriveSubsystem() {
        inputLimiterX = new SlewRateLimiter(1.0 / Constants.MecanumDrive.SECONDS_TO_ACCELERATE);
        inputLimiterY = new SlewRateLimiter(1.0 / Constants.MecanumDrive.SECONDS_TO_ACCELERATE);
        inputLimiterRotation = new SlewRateLimiter(1.0 / Constants.MecanumDrive.SECONDS_TO_ACCELERATE);

        driveMode = DriveMode.ThrowerOriented;

        configureMotors();

        mecanumDriveKinematics = new MecanumDriveKinematics(Constants.MecanumDrive.frontLeftWheelDisplacementMeters.get(),
                Constants.MecanumDrive.frontRightWheelDisplacementMeters.get(),
                Constants.MecanumDrive.backLeftWheelDisplacementMeters.get(),
                Constants.MecanumDrive.backRightWheelDisplacementMeters.get());
        wheelSpeeds = new MecanumDriveWheelSpeeds();

        drive = new MecanumDrive(frontleftMotor, backleftMotor, frontrightMotor, backrightMotor);
        drive.setRightSideInverted(false);

        navx = Robot.getNavx();
        odometry = new MecanumDriveOdometry(mecanumDriveKinematics, navx.getRotation2d(),
                new Pose2d(0, 0, new Rotation2d(0)));
    }

    public static MecanumDriveSubsystemBase getInstance() {
        if (instance == null) {
            if (Constants.MecanumDrive.IS_ENABLED) {
                instance = new MecanumDriveSubsystem();
                instance.setDefaultCommand(new DefaultDriveCommand());
                // if (!Constants.SwerveDrive.enabled)
                //     throw new Error("Swerve drive can't be enabled while swerve drive is enabled");
            } else
                instance = new MecanumDriveSubsystemBase();
        }
        return instance;
    }

    private void configureMotors() {
        frontrightMotor = Constants.MecanumDrive.frontRightMotorInitializer.get();
        backrightMotor = Constants.MecanumDrive.backRightMotorInitializer.get();
        frontleftMotor = Constants.MecanumDrive.frontLeftMotorInitializer.get();
        backleftMotor = Constants.MecanumDrive.backLeftMotorInitializer.get();

        frontleftMotor.setInverted(Constants.MecanumDrive.frontLeftMotorInverted);
        frontrightMotor.setInverted(Constants.MecanumDrive.frontRightMotorInverted);
        backleftMotor.setInverted(Constants.MecanumDrive.backLeftMotorInverted);
        backrightMotor.setInverted(Constants.MecanumDrive.backRightMotorInverted);

        frontrightMotor.configEncoder(FeedbackDevice.QuadEncoder, 1);
        backrightMotor.configEncoder(FeedbackDevice.QuadEncoder, 1);
        frontleftMotor.configEncoder(FeedbackDevice.QuadEncoder, 1);
        backleftMotor.configEncoder(FeedbackDevice.QuadEncoder, 1);

        frontleftMotor.setEncoderDirection(Constants.MecanumDrive.frontLeftEncoderInverted);
        frontrightMotor.setEncoderDirection(Constants.MecanumDrive.frontLeftEncoderInverted);
        backleftMotor.setEncoderDirection(Constants.MecanumDrive.backLeftEncoderInverted);
        backrightMotor.setEncoderDirection(Constants.MecanumDrive.frontLeftEncoderInverted);

        frontleftMotor.enableForwardLimitSwitch(LimitSwitchPolarity.kNormallyClosed, false);
        frontrightMotor.enableForwardLimitSwitch(LimitSwitchPolarity.kNormallyClosed, false);
        backleftMotor.enableForwardLimitSwitch(LimitSwitchPolarity.kNormallyClosed, false);
        backrightMotor.enableForwardLimitSwitch(LimitSwitchPolarity.kNormallyClosed, false);
    }

    private void resetEncoders() {
        frontleftMotor.setEncoderPosition(0);
        frontrightMotor.setEncoderPosition(0);
        backleftMotor.setEncoderPosition(0);
        backrightMotor.setEncoderPosition(0);
    }

    private double convertEncoderSpeedToMetersPerSecond(double encoderSpeed) {
        return encoderSpeed / Constants.MecanumDrive.ticksPerRotation * Constants.MecanumDrive.wheelDiameter * Math.PI;
    }

    @Override
    public void updateOdometry() {
        this.wheelSpeeds.frontLeftMetersPerSecond = convertEncoderSpeedToMetersPerSecond(
                frontleftMotor.getEncoderVelocity());
        this.wheelSpeeds.frontRightMetersPerSecond = convertEncoderSpeedToMetersPerSecond(
                frontrightMotor.getEncoderVelocity());
        this.wheelSpeeds.rearLeftMetersPerSecond = convertEncoderSpeedToMetersPerSecond(
                backleftMotor.getEncoderVelocity());
        this.wheelSpeeds.rearRightMetersPerSecond = convertEncoderSpeedToMetersPerSecond(
                backrightMotor.getEncoderVelocity());
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
        JoystickButton slowSpeedModeButton = new JoystickButton(joystick, Constants.MecanumDrive.ButtonIds.slowSpeedMode);
        JoystickButton fieldOrientedButton = new JoystickButton(joystick, Constants.MecanumDrive.ButtonIds.fieledOriented);
        JoystickButton pickUpOrientedButton = new JoystickButton(joystick, Constants.MecanumDrive.ButtonIds.fieledOriented);
        JoystickButton throwerOrientedButton = new JoystickButton(joystick, Constants.MecanumDrive.ButtonIds.fieledOriented);

        slowSpeedModeButton.whenPressed(() -> {
            if (speedFactor == Constants.MecanumDrive.slowModeSpeedFactor) 
                speedFactor = Constants.MecanumDrive.defaultSpeedFac1or;
            else
                speedFactor = Constants.MecanumDrive.slowModeSpeedFactor;
        });
        fieldOrientedButton.whenPressed(new FieldOriented());
        pickUpOrientedButton.whenPressed(new PickupOriented());
        throwerOrientedButton.whenPressed(new ThrowerOriented());
    }

    @Override
    public void setDriveMode(DriveMode driveMode) {
        this.driveMode = driveMode;
    }
}