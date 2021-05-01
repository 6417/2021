package frc.robot.subsystems.base;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.subsystems.swerve.SwerveModule;
import frc.robot.subsystems.swerve.SwerveDrive.DriveMode;

public class SwerveDriveBase extends SubsystemBase {
    public DriveMode getDriveMode() {
        return DriveMode.ThrowerOriented;
    }

    public void setDriveMode(DriveMode driveMode) {
        
    }

    public void drive(ChassisSpeeds requesteSpeeds) {

    }

    public void rotateAllModules(double speed) {

    }

    public Map<Constants.SwerveDrive.MountingLocations, Boolean> areHalSensoredOfMoudlesTriggered() {
        Map<Constants.SwerveDrive.MountingLocations, Boolean> defaultReturn = new HashMap<>();
        for (var location : Constants.SwerveDrive.MountingLocations.values())
            defaultReturn.put(location, false);
        return defaultReturn;
    }

    public void stopAllMotors() {

    }

    public void setCurrentModuleRotatoinToHome(Constants.SwerveDrive.MountingLocations moduleLocation) {

    }

    public void setModuleRotationEncoderTicks(Constants.SwerveDrive.MountingLocations mountingLocation, double ticks) {
        
    }

    public void forEachModule(Consumer<SwerveModule> consumer) {

    }

    public boolean areAllModulesZeroed() {
        return false;
    }

    public void forEachModuleEntry(
            Consumer<Map.Entry<Constants.SwerveDrive.MountingLocations, SwerveModule>> consumer) {
    }

    public boolean isModuleZeroed(Constants.SwerveDrive.MountingLocations mountingLocation) {
        return false;
    }

    public void withModule(Constants.SwerveDrive.MountingLocations mountingLocation, Consumer<SwerveModule> consumer) {

    }

    public void setSpeedFactor(double speedFactor) {
        
    }
}
