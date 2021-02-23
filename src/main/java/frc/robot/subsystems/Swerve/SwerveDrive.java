package frc.robot.subsystems.Swerve;

import java.util.HashMap;

import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.kinematics.SwerveDriveKinematics;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import frc.robot.Constants;
import frc.robot.subsystems.Base.SwerveDriveBase;

public class SwerveDrive extends SwerveDriveBase {
    private static SwerveDriveBase instance = null;
    private SwerveDriveKinematics kinematics;
    private HashMap<SwerveModule.MountingLocation, SwerveModule> modules = new HashMap<>();

    private SwerveDrive() {
        for (var location : SwerveModule.MountingLocation.values())
            modules.put(location, new SwerveModule(Constants.SwerveDrive.swerveModuleConfigs.get(location)));
        Translation2d[] mountingPoints = Constants.SwerveDrive.swerveModuleConfigs.values().stream()
                .map((SwerveModule.Config config) -> config.mountingPoint).toArray(Translation2d[]::new);
        kinematics = new SwerveDriveKinematics(mountingPoints);
    }

    public static SwerveDriveBase getInstance() {
        if (instance == null)
            if (Constants.SwerveDrive.enabled)
                instance = new SwerveDrive();
            else
                instance = new SwerveDriveBase();
        return instance;
    }

    public void drive(ChassisSpeeds requestedMovement) {
        SwerveModuleState[] states = kinematics.toSwerveModuleStates(requestedMovement);
        SwerveModule[] moduleArray = modules.values().toArray(SwerveModule[]::new);
        for (int i = 0; i < states.length; i++)
            moduleArray[i].setDesiredState(states[i]);
    }
}