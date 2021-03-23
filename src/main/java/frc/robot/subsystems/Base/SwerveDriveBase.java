package frc.robot.subsystems.Base;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.subsystems.Swerve.SwerveModule;

public class SwerveDriveBase extends SubsystemBase {
    public void drive(ChassisSpeeds requesteSpeeds) {

    }

    public void rotateAllModules(double speed) {

    }

    public HashMap<Constants.SwerveDrive.MountingLocations, Boolean> areHalSensoredOfMoudlesTriggered() {
        HashMap<Constants.SwerveDrive.MountingLocations, Boolean> defaultReturn = new HashMap<>();
        for (var location : Constants.SwerveDrive.MountingLocations.values())
            defaultReturn.put(location, false);
        return defaultReturn;
    }

    public void stopAllMotors() {

    }

    public void setCurrentModuleRotatoinToHome(Constants.SwerveDrive.MountingLocations moduleLocation) {

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
}
