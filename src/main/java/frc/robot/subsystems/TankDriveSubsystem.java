package frc.robot.subsystems;

import com.revrobotics.CANDigitalInput;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveKinematics;
import frc.robot.Constants;
import frc.robot.commands.DefaultDriveCommand;
import frc.robot.subsystems.Base.TankDriveSubsystemBase;
import frc.robot.utilities.Controller;
import frc.robot.utilities.fridolinsMotor.FridoCANSparkMax;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.IdleModeType;
import frc.robot.utilities.fridolinsMotor.FridolinsMotor.LimitSwitchPolarity;

public class TankDriveSubsystem extends TankDriveSubsystemBase {
    private static TankDriveSubsystemBase instance;
    private FridolinsMotor frontrightMotor;
    private FridolinsMotor backrightMotor;
    private FridolinsMotor frontleftMotor;
    private FridolinsMotor backleftMotor;
    private SpeedControllerGroup rightMotors;
    private SpeedControllerGroup leftMotors;
    private MecanumDrive drive;
    
    private TankDriveSubsystem() {
        configureMotors();
        drive = new MecanumDrive(frontleftMotor, frontrightMotor, backleftMotor, backleftMotor);
    }

    private void configureMotors() {
        frontrightMotor = Constants.TankDrive.frontRightMotorInitializer.get();
        backrightMotor = Constants.TankDrive.backRightMotorInitializer.get();
        frontleftMotor = Constants.TankDrive.frontLeftMotorInitializer.get();
        backleftMotor = Constants.TankDrive.backLeftMotorInitializer.get();

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
        drive.drivePolar(magnitude, angle, zRotation);;
    } 

}