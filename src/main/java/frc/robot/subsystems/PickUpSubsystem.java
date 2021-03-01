package frc.robot.subsystems;

import edu.wpi.first.wpilibj.SpeedControllerGroup;
import frc.robot.Constants;
import frc.robot.subsystems.Base.PickUpBase;
import frc.robot.utilities.fridolinsMotor.FridoCANSparkMax;
import frc.robot.utilities.Controller;
import frc.robot.utilities.Controller.ControlJoystick;

public class PickUpSubsystem {

    private static PickUpSubsystem m_Instance;
    private static FridoCANSparkMax leftMotor;
    private static FridoCANSparkMax rightMotor;
    private static FridoCANSparkMax [] motors = {leftMotor, rightMotor};
    private static ControlJoystick joystick;
    private static SpeedControllerGroup speedControllerGroupLeftRight;


    public PickUpSubsystem(){
        leftMotor = Constants.BallPickUp.pickUpMotorLeft.get();
        rightMotor = Constants.BallPickUp.pickUpMotorRight.get();

        for(var motor : motors){ // set factory defaults
            motor.factoryDefault();
        }

        speedControllerGroupLeftRight = new SpeedControllerGroup(leftMotor, rightMotor);

        joystick = Controller.getInstance().controlJoystick; // lol
    }

    public static void getInput(){
    }


    public static void setPickUpMotor(){
    }

    public static void stopMotor(){
        speedControllerGroupLeftRight.stopMotor();
    }

    public PickUpSubsystem getInstance(){
        if(m_Instance == null && Constants.BallPickUp.isBallPickUpSystemEnabled){
                m_Instance = new PickUpSubsystem();
        }
        else
            m_Instance = new PickUpBase();

        return m_Instance;
    }
}
