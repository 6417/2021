package frc.robot.subsystems;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.SlewRateLimiter;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveKinematics;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveOdometry;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveWheelSpeeds;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants;
import frc.robot.Robot;
import frc.robot.commands.DefaultDriveCommand;
import frc.robot.subsystems.base.MecanumDriveSubsystemBase;
import frc.robot.utilities.ShuffleBoardInformation;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.FeedbackDevice;

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

    private MecanumDriveSubsystem() {
        inputLimiterX = new SlewRateLimiter(1.0 / Constants.TankDrive.SECONDS_TO_ACCELERATE);
        inputLimiterY = new SlewRateLimiter(1.0 / Constants.TankDrive.SECONDS_TO_ACCELERATE);
        inputLimiterRotation = new SlewRateLimiter(1.0 / Constants.TankDrive.SECONDS_TO_ACCELERATE);

        driveMode = DriveMode.RobotOriented;

        configureMotors();
        
        mecanumDriveKinematics = new MecanumDriveKinematics(Constants.TankDrive.frontLeftWheelDisplacementMeters.get(), Constants.TankDrive.frontRightWheelDisplacementMeters.get(), Constants.TankDrive.backLeftWheelDisplacementMeters.get(), Constants.TankDrive.backRightWheelDisplacementMeters.get());
        wheelSpeeds = new MecanumDriveWheelSpeeds();
        
        drive = new MecanumDrive(frontleftMotor, backleftMotor, frontrightMotor, backrightMotor);
        drive.setRightSideInverted(false);

        navx = Robot.getNavx();
        odometry = new MecanumDriveOdometry(mecanumDriveKinematics, navx.getRotation2d(), new Pose2d(0, 0, new Rotation2d(0)));
    }

    public static MecanumDriveSubsystemBase getInstance() {
        if (instance == null) {
            if (Constants.TankDrive.IS_ENABLED) {
                instance = new MecanumDriveSubsystem();
                instance.setDefaultCommand(new DefaultDriveCommand());
            }
            else
                instance = new MecanumDriveSubsystemBase();
        }
        return instance;
    }

    private void configureMotors() {
        frontrightMotor = Constants.TankDrive.frontRightMotorInitializer.get();
        backrightMotor = Constants.TankDrive.backRightMotorInitializer.get();
        frontleftMotor = Constants.TankDrive.frontLeftMotorInitializer.get();
        backleftMotor = Constants.TankDrive.backLeftMotorInitializer.get();

        frontleftMotor.setInverted(false);

        frontrightMotor.configEncoder(FeedbackDevice.QuadEncoder, 1);
        backrightMotor.configEncoder(FeedbackDevice.QuadEncoder, 1);
        frontleftMotor.configEncoder(FeedbackDevice.QuadEncoder, 1);
        backleftMotor.configEncoder(FeedbackDevice.QuadEncoder, 1);

        backleftMotor.setEncoderDirection(true);
        frontleftMotor.setEncoderDirection(true);

        // FridolinsMotor[] motors = {frontrightMotor, backrightMotor, frontleftMotor, backleftMotor};
        // for (FridoCANSparkMax motor: motors){
        //     motor.restoreFactoryDefaults();
        //     CANDigitalInput forwardLimitSwitch = motor.getForwardLimitSwitch(com.revrobotics.CANDigitalInput.LimitSwitchPolarity.kNormallyOpen);
        //     CANDigitalInput reverseLimitSwitch = motor.getReverseLimitSwitch(com.revrobotics.CANDigitalInput.LimitSwitchPolarity.kNormallyOpen);
        //     forwardLimitSwitch.enableLimitSwitch(true);
        //     reverseLimitSwitch.enableLimitSwitch(true);
        //     motor.setIdleMode(IdleMode.kBrake);
        //     CANEncoder encoder = motor.getEncoder();
        //     System.out.println("Configured Motor");
        // }
        // frontrightMotor.factoryDefault();
        // frontrightMotor.enableForwardLimitSwitch(LimitSwitchPolarity.kNormallyOpen, true);
        // frontrightMotor.enableReverseLimitSwitch(LimitSwitchPolarity.kNormallyOpen, true);
        // frontrightMotor.setIdleMode(IdleModeType.kBrake);

        // frontleftMotor.factoryDefault();
        // frontleftMotor.enableForwardLimitSwitch(LimitSwitchPolarity.kNormallyOpen, true);
        // frontleftMotor.enableReverseLimitSwitch(LimitSwitchPolarity.kNormallyOpen, true);
        // frontleftMotor.setIdleMode(IdleModeType.kBrake);

        // backrightMotor.factoryDefault();
        // backrightMotor.enableForwardLimitSwitch(LimitSwitchPolarity.kNormallyOpen, true);
        // backrightMotor.enableReverseLimitSwitch(LimitSwitchPolarity.kNormallyOpen, true);
        // backrightMotor.setIdleMode(IdleModeType.kBrake);

        // backleftMotor.factoryDefault();
        // backleftMotor.enableForwardLimitSwitch(LimitSwitchPolarity.kNormallyOpen, true);
        // backleftMotor.enableReverseLimitSwitch(LimitSwitchPolarity.kNormallyOpen, true);
        // backleftMotor.setIdleMode(IdleModeType.kBrake);
        

    }

    private void resetEncoders() {
        frontleftMotor.setEncoderPosition(0);
        frontrightMotor.setEncoderPosition(0);
        backleftMotor.setEncoderPosition(0);
        backrightMotor.setEncoderPosition(0);
    }

    private double convertEncoderSpeedToMetersPerSecond(double encoderSpeed) {
        return encoderSpeed / Constants.TankDrive.ticksPerRotation * Constants.TankDrive.wheelDiameter * Math.PI;
    }

    @Override
    public void updateOdometry() {
        this.wheelSpeeds.frontLeftMetersPerSecond = convertEncoderSpeedToMetersPerSecond(frontleftMotor.getEncoderVelocity());
        this.wheelSpeeds.frontRightMetersPerSecond = convertEncoderSpeedToMetersPerSecond(frontrightMotor.getEncoderVelocity());
        this.wheelSpeeds.rearLeftMetersPerSecond = convertEncoderSpeedToMetersPerSecond(backleftMotor.getEncoderVelocity());
        this.wheelSpeeds.rearRightMetersPerSecond = convertEncoderSpeedToMetersPerSecond(backrightMotor.getEncoderVelocity());
        odometry.update(navx.getRotation2d(), this.wheelSpeeds);
    }

    @Override
    public void resetOdometry() {
        navx.reset();
        resetEncoders();
        odometry.resetPosition(new Pose2d((double)0, (double)0, new Rotation2d(0)), navx.getRotation2d());
    }

    @Override
    public Pose2d getPosition() {
        return odometry.getPoseMeters();
    }

    @Override
    public void toggleDriveMode() {
        switch (driveMode)
        {
            case FieldOriented:
                driveMode = DriveMode.RobotOriented;
                break;
            case RobotOriented:
                driveMode = DriveMode.FieldOriented;
                break;
        }    
    }

    @Override
    public void drive(double xSpeed, double ySpeed, double zRotation){
        switch (driveMode)
        {
            case RobotOriented:
                drive.driveCartesian(inputLimiterX.calculate(xSpeed), inputLimiterY.calculate(-ySpeed), inputLimiterRotation.calculate(zRotation));
                break;
            case FieldOriented:
                drive.driveCartesian(inputLimiterX.calculate(xSpeed), inputLimiterY.calculate(-ySpeed), inputLimiterRotation.calculate(zRotation), navx.getAngle());
                break;
        }
        this.updateOdometry();
    } 

    @Override
    public void initSendable(SendableBuilder builder) {
        builder.addStringProperty("driveMode", () -> driveMode.toString(), null);
        super.initSendable(builder);
    }
}