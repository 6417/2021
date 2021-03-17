package frc.robot.subsystems;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants;
import frc.robot.commands.DefaultDriveCommand;
import frc.robot.subsystems.Base.TankDriveSubsystemBase;
import frc.robot.utilities.Vector2d;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;

public class TankDriveSubsystem extends TankDriveSubsystemBase {
    private static TankDriveSubsystemBase instance;
    private FridolinsMotor frontrightMotor;
    private FridolinsMotor backrightMotor;
    private FridolinsMotor frontleftMotor;
    private FridolinsMotor backleftMotor;
    private SpeedControllerGroup rightMotors;
    private SpeedControllerGroup leftMotors;
    private MecanumDrive drive;
    private Encoder encoderFrontLeft;
    
    private Vector2d joystickVector;

    private TankDriveSubsystem() {
        encoderFrontLeft = new Encoder(2, 3);
        configureMotors();
        drive = new MecanumDrive(frontleftMotor, backleftMotor, frontrightMotor, backrightMotor);
        drive.setRightSideInverted(false);
        joystickVector  = new Vector2d(); 
    }

    private void configureMotors() {
        frontrightMotor = Constants.TankDrive.frontRightMotorInitializer.get();
        backrightMotor = Constants.TankDrive.backRightMotorInitializer.get();
        frontleftMotor = Constants.TankDrive.frontLeftMotorInitializer.get();
        backleftMotor = Constants.TankDrive.backLeftMotorInitializer.get();
        frontleftMotor.setInverted(true);

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
    public void drive(double xSpeed, double ySpeed, double zRotation){
        joystickVector.x = xSpeed;
        joystickVector.y = ySpeed;
        drive.driveCartesian(xSpeed, -ySpeed, zRotation);
    } 


}