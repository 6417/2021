package frc.robot.subsystems.base;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.subsystems.Drive.DriveMode;
import frc.robot.subsystems.swerve.SwerveModule;

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

    public Map<Constants.Drive.MountingLocations, Boolean> areHalSensoredOfMoudlesTriggered() {
        Map<Constants.Drive.MountingLocations, Boolean> defaultReturn = new HashMap<>();
        for (var location : Constants.Drive.MountingLocations.values())
            defaultReturn.put(location, false);
        return defaultReturn;
    }

    public void stopAllMotors() {

    }

    public void setCurrentModuleRotatoinToHome(Constants.Drive.MountingLocations moduleLocation) {

    }

    public void setModuleRotationEncoderTicks(Constants.Drive.MountingLocations mountingLocation, double ticks) {
        
    }

    public void forEachModule(Consumer<SwerveModule> consumer) {

    }

    public boolean areAllModulesZeroed() {
        return false;
    }

    public void forEachModuleEntry(
            Consumer<Map.Entry<Constants.Drive.MountingLocations, SwerveModule>> consumer) {
    }

    public boolean isModuleZeroed(Constants.Drive.MountingLocations mountingLocation) {
        return false;
    }

    public void withModule(Constants.Drive.MountingLocations mountingLocation, Consumer<SwerveModule> consumer) {

    }

    public void setSpeedFactor(double speedFactor) {
        
    }

    public void configureButtonBindings(Joystick joystick) {
        
    }
}
