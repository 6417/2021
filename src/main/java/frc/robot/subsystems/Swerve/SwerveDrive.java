package frc.robot.subsystems.swerve;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SendableRegistry;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants;
import frc.robot.Constants.SwerveDrive.MountingLocations;
import frc.robot.commands.swerve.DefaultDriveCommand;
import frc.robot.subsystems.base.SwerveDriveBase;
import frc.robot.utilities.SwerveKinematics;
import frc.robot.utilities.swerveLimiter.SwerveLimiter;
import frc.robot.utilities.swerveLimiter.SwerveLimiterBase;

public class SwerveDrive extends SwerveDriveBase {
    public static enum DriveMode {
        ThrowerOriented, PickupOriented, FieldOriented;
    }

    private DriveMode driveMode = DriveMode.ThrowerOriented;
    private static SwerveDriveBase instance = null;
    private SwerveKinematics<Constants.SwerveDrive.MountingLocations> kinematics;
    private HashMap<Constants.SwerveDrive.MountingLocations, SwerveModule> modules = new HashMap<>();
    private SwerveLimiter.RotationDirectionCorrectorGetter<Constants.SwerveDrive.MountingLocations> directionCorectorGetter;
    private ChassisSpeeds currentChassisSpeeds = new ChassisSpeeds();
    private double speedFactor = Constants.SwerveDrive.defaultSpeedFactor;

    private void setUpSwerveKinematics() {
        HashMap<Constants.SwerveDrive.MountingLocations, Translation2d> mountingPoints = new HashMap<>();
        for (var element : Constants.SwerveDrive.swerveModuleConfigs.entrySet())
            mountingPoints.put(element.getKey(), element.getValue().mountingPoint);
        kinematics = new SwerveKinematics<Constants.SwerveDrive.MountingLocations>(mountingPoints);
    }

    private void setUpSwerveModules() {
        for (var location : Constants.SwerveDrive.MountingLocations.values())
            modules.put(location, new SwerveModule(Constants.SwerveDrive.swerveModuleConfigs.get(location)));
        forEachModuleEntry((moduleEntry) -> SendableRegistry.addLW(moduleEntry.getValue(),
                "SwerveModule " + moduleEntry.getKey().toString()));
    }

    private SwerveDrive() {
        setUpSwerveModules();
        setUpSwerveKinematics();
        directionCorectorGetter = Constants.SwerveDrive.directionCorectorGetter;
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
    public boolean isModuleZeroed(Constants.SwerveDrive.MountingLocations mountingLocation) {
        return modules.get(mountingLocation).hasEncoderBeenZeroed();
    }

    @Override
    public void withModule(Constants.SwerveDrive.MountingLocations mountingLocation, Consumer<SwerveModule> consumer) {
        consumer.accept(modules.get(mountingLocation));
    }

    private Map<Constants.SwerveDrive.MountingLocations, SwerveModuleState> normalizeStates(
            Map<Constants.SwerveDrive.MountingLocations, SwerveModuleState> states) {
        double maxOfCurrentSpeeds = states.values().stream()
                .max(Comparator.comparing((state) -> state.speedMetersPerSecond)).get().speedMetersPerSecond;
        if (maxOfCurrentSpeeds > Constants.SwerveDrive.maxSpeedOfDrive * speedFactor) {
            Map<Constants.SwerveDrive.MountingLocations, SwerveModuleState> normalizedStates = new HashMap<>();
            for (var stateEntry : states.entrySet()) {
                SwerveModuleState normalizedState = stateEntry.getValue();
                normalizedState.speedMetersPerSecond /= maxOfCurrentSpeeds;
                normalizedState.speedMetersPerSecond *= speedFactor;
                normalizedStates.put(stateEntry.getKey(), normalizedState);
            }
            return normalizedStates;
        } else
            return states;
    }

    @Override
    public void drive(ChassisSpeeds requestedMovement) {
        currentChassisSpeeds = requestedMovement;
        Map<Constants.SwerveDrive.MountingLocations, SwerveModuleState> states = kinematics
                .toLabledSwerveModuleStates(requestedMovement);
        states = normalizeStates(states);
        // Map<Constants.SwerveDrive.MountingLocations, Double> rotationOfsetFactors =
        // SwerveLimiterBase
        // .getRotationOfsets(modules, states);

        states.entrySet()
                .forEach((Entry<Constants.SwerveDrive.MountingLocations, SwerveModuleState> labeledState) -> modules
                        .get(labeledState.getKey()).setDesiredState(labeledState.getValue(),
                                1.0 /* rotationOfsetFactors.get(labeledState.getKey()) */));

        if (Constants.SwerveDrive.rotateAllModulesInSameDirection)
            correctRotationDirections(Math.abs(requestedMovement.omegaRadiansPerSecond) > 0.01);

        forEachModule((module) -> module.drive(speedFactor));
    }

    private Map<Constants.SwerveDrive.MountingLocations, SwerveLimiter.ModuleRotationVectors> getModuleRotationVectorMap() {
        return modules.entrySet().stream().map((
                Entry<Constants.SwerveDrive.MountingLocations, SwerveModule> entry) -> new AbstractMap.SimpleEntry<Constants.SwerveDrive.MountingLocations, SwerveLimiter.ModuleRotationVectors>(
                        entry.getKey(),
                        new SwerveLimiter.ModuleRotationVectors(entry.getValue().getModuleRotation(),
                                entry.getValue().getTargetVector())))
                .collect(Collectors.toMap(
                        Entry::getKey,Entry::getValue));
    }

    @Override
    public void setCentricSwerveMode(boolean on) {
        forEachModule((module) -> module.setCentricSwerveMode(on));
    }

    private void correctRotationDirections(boolean isRobotRotating) {
        Map<Constants.SwerveDrive.MountingLocations, SwerveLimiter.ModuleRotationVectors> moduleRotatoinVectors = getModuleRotationVectorMap();
        Map<Constants.SwerveDrive.MountingLocations, Optional<Boolean>> corrections = SwerveLimiter
                .getModuleRotaionDirectionCorrections(moduleRotatoinVectors, isRobotRotating);
        corrections.entrySet().stream()
                .filter((correctionEntry) -> correctionEntry.getValue().isPresent())
                .map((correctionEntry) -> new AbstractMap.SimpleEntry<>(correctionEntry.getKey(),
                        correctionEntry.getValue().get()))
                .filter((correctionEntry) -> correctionEntry.getValue())
                .forEach((correctionEntry) -> modules.get(correctionEntry.getKey()).invertRotationDirection());

        SmartDashboard.putStringArray("Corrections", corrections.values().stream().map((Optional<Boolean> correction) -> correction.toString()).toArray(String[]::new));
    }

    @Override
    public void rotateAllModules(double speed) {
        for (var module : modules.values())
            module.rotateModule(speed);
    }

    @Override
    public HashMap<Constants.SwerveDrive.MountingLocations, Boolean> areHalSensoredOfMoudlesTriggered() {
        HashMap<Constants.SwerveDrive.MountingLocations, Boolean> result = new HashMap<>();
        forEachModuleEntry(
                (labeledModule) -> result.put(labeledModule.getKey(), labeledModule.getValue().isHalSensorTriggered()));
        return result;
    }

    @Override
    public void setCurrentModuleRotatoinToHome(MountingLocations moduleLocation) {
        modules.get(moduleLocation).setCurrentRotationToEncoderHome();
    }

    public static double joystickInputToMetersPerSecond(double joystickValue) {
        return joystickValue * Constants.SwerveDrive.maxSpeedOfDrive;
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

    @Override
    public void forEachModuleEntry(
            Consumer<Map.Entry<Constants.SwerveDrive.MountingLocations, SwerveModule>> consumer) {
        modules.entrySet().stream().forEach(consumer);
    }

    @Override
    public void setModuleRotationEncoderTicks(MountingLocations mountingLocation, double ticks) {
        modules.get(mountingLocation).setRotationEncoderTicks(ticks);
    }

    @Override
    public void setSpeedFactor(double speedFactor) {
        assert speedFactor > 0.0 : "speedFactor must be grater than zero";
        this.speedFactor = speedFactor;
    }

    @Override
    public DriveMode getDriveMode() {
        return driveMode;
    }

    @Override
    public void setDriveMode(DriveMode driveMode) {
        this.driveMode = driveMode;
    }

    @Override
    public void activateBreak() {
        forEachModule((module) -> module.activateBreak());
    }

    @Override
    public void deactivateBreak() {
        forEachModule((module) -> module.deactivateBreak());
    }
}
