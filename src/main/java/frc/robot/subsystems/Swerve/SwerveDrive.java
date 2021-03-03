package frc.robot.subsystems.Swerve;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SendableRegistry;
import frc.robot.Constants;
import frc.robot.Constants.SwerveDrive.MountingLocations;
import frc.robot.commands.Swerve.DefaultDriveCommand;
import frc.robot.subsystems.Base.SwerveDriveBase;
import frc.robot.utilities.SwerveKinematics;
import frc.robot.utilities.swerveLimiter.SwerveLimiter;

public class SwerveDrive extends SwerveDriveBase {
    private static SwerveDriveBase instance = null;
    private SwerveKinematics<Constants.SwerveDrive.MountingLocations> kinematics;
    private HashMap<Constants.SwerveDrive.MountingLocations, SwerveModule> modules = new HashMap<>();
    private SwerveLimiter.RotationDirectionCorrectorGetter<Constants.SwerveDrive.MountingLocations> directionCorectorGetter;
    private ChassisSpeeds currentChassisSpeeds = new ChassisSpeeds();

    private SwerveDrive() {
        for (var location : Constants.SwerveDrive.MountingLocations.values())
            modules.put(location, new SwerveModule(Constants.SwerveDrive.swerveModuleConfigs.get(location)));
        HashMap<Constants.SwerveDrive.MountingLocations, Translation2d> mountingPoints = new HashMap<>();
        for (var element : Constants.SwerveDrive.swerveModuleConfigs.entrySet())
            mountingPoints.put(element.getKey(), element.getValue().mountingPoint);
        kinematics = new SwerveKinematics<Constants.SwerveDrive.MountingLocations>(mountingPoints);
        directionCorectorGetter = Constants.SwerveDrive.directionCorectorGetter;
        for (var moduleEntry : modules.entrySet())
            SendableRegistry.addLW(moduleEntry.getValue(), "Swerve Module " + moduleEntry.getKey().toString());
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
        currentChassisSpeeds = requestedMovement;
        HashMap<Constants.SwerveDrive.MountingLocations, SwerveModuleState> states = kinematics
                .toLabledSwerveModuleStates(requestedMovement);
        for (var labeledState : states.entrySet())
            modules.get(labeledState.getKey()).setDesiredState(labeledState.getValue());
        correctRotationDirections(requestedMovement.omegaRadiansPerSecond == 0.0);
        for (var module : modules.values())
            module.drive();
    }

    private Map<Constants.SwerveDrive.MountingLocations, SwerveLimiter.ModuleRotationVectors> getModuleRotationVectorMap() {
        return modules.entrySet().stream().map((
                Entry<Constants.SwerveDrive.MountingLocations, SwerveModule> entry) -> new AbstractMap.SimpleEntry<Constants.SwerveDrive.MountingLocations, SwerveLimiter.ModuleRotationVectors>(
                        entry.getKey(),
                        new SwerveLimiter.ModuleRotationVectors(entry.getValue().getModuleRotation(),
                                entry.getValue().getTargetVector())))
                .collect(Collectors.toMap(
                        Entry<Constants.SwerveDrive.MountingLocations, SwerveLimiter.ModuleRotationVectors>::getKey,
                        Entry<Constants.SwerveDrive.MountingLocations, SwerveLimiter.ModuleRotationVectors>::getValue));
    }

    private void correctRotationDirections(boolean isRobotRotating) {
        Map<Constants.SwerveDrive.MountingLocations, SwerveLimiter.ModuleRotationVectors> moduleRotatoinVectors = getModuleRotationVectorMap();
        Map<Constants.SwerveDrive.MountingLocations, Boolean> corrections = directionCorectorGetter
                .getModuleRotationDirectionCorrections(moduleRotatoinVectors, isRobotRotating);
        corrections.entrySet().stream().filter((correctionEntry) -> correctionEntry.getValue())
                .forEach((correctionEntry) -> modules.get(correctionEntry.getKey()).invertRotationDirection());
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
    public void forEachModule(Consumer<SwerveModule> consumer) {
        modules.values().stream().forEach(consumer);
    }

    @Override
    public void stopAllMotors() {
        for (var module : modules.values())
            module.stopAllMotors();
    }

    @Override
    public boolean areAllModulesZeroed() {
        boolean result = true;
        for (var module : modules.values())
            result = result && module.hasEncoderBeenZeroed();
        return result;
    }

    @Override
    public void initSendable(SendableBuilder builder) {
        super.initSendable(builder);
        builder.addDoubleProperty("Current chassi speed x", () -> currentChassisSpeeds.vxMetersPerSecond, null);
        builder.addDoubleProperty("Current chassi speed y", () -> currentChassisSpeeds.vxMetersPerSecond, null);
        builder.addDoubleProperty("Current chassi speed rotation", () -> currentChassisSpeeds.vxMetersPerSecond, null);
    }
}