package frc.robot.subsystems.Swerve;

import java.util.HashMap;

import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import frc.robot.Constants;
import frc.robot.Constants.SwerveDrive.MountingLocations;
import frc.robot.commands.Swerve.DefaultDriveCommand;
import frc.robot.subsystems.Base.SwerveDriveBase;
import frc.robot.utilities.SwerveKinematics;

public class SwerveDrive extends SwerveDriveBase {
    private static SwerveDriveBase instance = null;
    private SwerveKinematics<Constants.SwerveDrive.MountingLocations> kinematics;
    private HashMap<Constants.SwerveDrive.MountingLocations, SwerveModule> modules = new HashMap<>();

    private SwerveDrive() {
        for (var location : Constants.SwerveDrive.MountingLocations.values())
            modules.put(location, new SwerveModule(Constants.SwerveDrive.swerveModuleConfigs.get(location)));
        HashMap<Constants.SwerveDrive.MountingLocations, Translation2d> mountingPoints = new HashMap<>();
        for (var element : Constants.SwerveDrive.swerveModuleConfigs.entrySet())
            mountingPoints.put(element.getKey(), element.getValue().mountingPoint);
        kinematics = new SwerveKinematics<Constants.SwerveDrive.MountingLocations>(mountingPoints);
    }

    public static SwerveDriveBase getInstance() {
        if (instance == null)
            if (Constants.SwerveDrive.enabled) {
                instance = new SwerveDrive();
                instance.setDefaultCommand(new DefaultDriveCommand());
            } else
                instance = new SwerveDriveBase();
        return instance;
    }

    @Override
    public void drive(ChassisSpeeds requestedMovement) {
        HashMap<Constants.SwerveDrive.MountingLocations, SwerveModuleState> states = kinematics
                .toLabledSwerveModuleStates(requestedMovement);
        for (var labeledState : states.entrySet())
            modules.get(labeledState.getKey()).setDesiredState(labeledState.getValue());
    }

    @Override
    public void rotateAllModules(double speed) {
        for (var module : modules.values())
            module.rotateModule(speed);
    }

    @Override
    public HashMap<Constants.SwerveDrive.MountingLocations, Boolean> areHalSensoredOfMoudlesTriggered() {
        HashMap<Constants.SwerveDrive.MountingLocations, Boolean> result = new HashMap<>();
        for (var labeledModule : modules.entrySet())
            result.put(labeledModule.getKey(), labeledModule.getValue().isHalSensorTriggered());
        return result;
    }

    @Override
    public void setCurrentModuleRotatoinToHome(MountingLocations moduleLocation) {
        modules.get(moduleLocation).setCurrentRotationToEncoderHome();
    }

    public static double joystickInputToMetersPerSecond(double joystickValue) {
        return (((joystickValue * Constants.SwerveDrive.maxSpeedOfDrive)
                / Constants.SwerveDrive.commonConfigurations.driveMotorTicksPerRotation))
                * Constants.SwerveDrive.commonConfigurations.wheelCircumference;
    }

    public static double joystickInputToRadPerSecond(double joystickValue) {
        return joystickValue * Constants.SwerveDrive.maxRotationSpeed;
    }

    @Override
    public void stopAllMotors() {
        for (var module : modules.values())
            module.stopAllMotors();
    }
}