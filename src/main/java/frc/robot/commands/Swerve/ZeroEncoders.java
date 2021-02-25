package frc.robot.commands.Swerve;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.subsystems.Swerve.SwerveDrive;

public class ZeroEncoders extends CommandBase {
    public ZeroEncoders() {
        addRequirements(SwerveDrive.getInstance());
    }

    @Override
    public void initialize() {
        SwerveDrive.getInstance().rotateAllModules(Constants.SwerveDrive.zeroingSpeed);  
    }

    @Override
    public void execute() {
        for (var isHalSensorTriggeredOfMoudle : SwerveDrive.getInstance().areHalSensoredOfMoudlesTriggered().entrySet())
            if (isHalSensorTriggeredOfMoudle.getValue())
                SwerveDrive.getInstance().setCurrentModuleRotatoinToHome(isHalSensorTriggeredOfMoudle.getKey());
    }

    @Override
    public boolean isFinished() {
        return SwerveDrive.getInstance().areAllModulesZeroed();
    }

    @Override
    public void end(boolean interrupted) {
        SwerveDrive.getInstance().stopAllMotors();
    }
}