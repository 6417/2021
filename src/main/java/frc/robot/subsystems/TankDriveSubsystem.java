package frc.robot.subsystems;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveKinematics;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveOdometry;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveWheelSpeeds;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants;
import frc.robot.commands.DefaultDriveCommand;
import frc.robot.subsystems.Base.TankDriveSubsystemBase;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.FeedbackDevice;

public class TankDriveSubsystem extends TankDriveSubsystemBase {
    private static TankDriveSubsystemBase instance;
    private FridolinsMotor frontrightMotor;
    private FridolinsMotor backrightMotor;
    private FridolinsMotor frontleftMotor;
    private FridolinsMotor backleftMotor;
    private MecanumDrive drive;
    private MecanumDriveKinematics mecanumDriveKinematics;
    private MecanumDriveOdometry odometry;
    private MecanumDriveWheelSpeeds wheelSpeeds;
    private AHRS navx;

    private TankDriveSubsystem() {
        configureMotors();
        drive = new MecanumDrive(frontleftMotor, backleftMotor, frontrightMotor, backrightMotor);
        drive.setRightSideInverted(false);
        mecanumDriveKinematics = new MecanumDriveKinematics(Constants.TankDrive.frontLeftWheelDisplacementMeters.get(), Constants.TankDrive.frontRightWheelDisplacementMeters.get(), Constants.TankDrive.backLeftWheelDisplacementMeters.get(), Constants.TankDrive.backRightWheelDisplacementMeters.get());
        wheelSpeeds = new MecanumDriveWheelSpeeds();
        navx = Constants.TankDrive.navxInitializer.get();
        odometry = new MecanumDriveOdometry(mecanumDriveKinematics, navx.getRotation2d(), new Pose2d(0, 0, new Rotation2d(0)));
    }

    private void configureMotors() {
        frontrightMotor = Constants.TankDrive.frontRightMotorInitializer.get();
        backrightMotor = Constants.TankDrive.backRightMotorInitializer.get();
        frontleftMotor = Constants.TankDrive.frontLeftMotorInitializer.get();
        backleftMotor = Constants.TankDrive.backLeftMotorInitializer.get();

        frontrightMotor.configEncoder(FeedbackDevice.QuadEncoder, 1);
        backrightMotor.configEncoder(FeedbackDevice.QuadEncoder, 1);
        frontleftMotor.configEncoder(FeedbackDevice.QuadEncoder, 1);
        backleftMotor.configEncoder(FeedbackDevice.QuadEncoder, 1);

        frontleftMotor.setDirection(false);
        frontleftMotor.setEncoderDirection(true);
        backleftMotor.setEncoderDirection(true);

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

    public static TankDriveSubsystemBase getInstance() {
        if (instance == null) {
            if (Constants.TankDrive.IS_ENABLED) {
                instance = new TankDriveSubsystem();
                instance.setDefaultCommand(new DefaultDriveCommand());
            }
            else
                instance = new TankDriveSubsystemBase();
        }
        return instance;
    }

    @Override
    public void updateOdometry() {
        
    }

    @Override
    public void drive(double xSpeed, double ySpeed, double zRotation){
        //drive.driveCartesian(xSpeed, -ySpeed, zRotation);
        SmartDashboard.putNumber("EncoderFrontLeft", frontleftMotor.getEncoderTicks());
        SmartDashboard.putNumber("EncoderBackLeft", backleftMotor.getEncoderTicks());
        SmartDashboard.putNumber("EncoderFrontRight", frontrightMotor.getEncoderTicks());
        SmartDashboard.putNumber("EncoderBackRight", backrightMotor.getEncoderTicks());
    } 
}