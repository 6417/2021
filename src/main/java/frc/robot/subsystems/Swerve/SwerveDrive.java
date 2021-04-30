package frc.robot.subsystems.swerve;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SendableRegistry;
import frc.robot.Constants;
import frc.robot.Constants.SwerveDrive.MountingLocations;
import frc.robot.Robot;
import frc.robot.commands.swerve.DefaultDriveCommand;
import frc.robot.subsystems.base.SwerveDriveBase;
import frc.robot.utilities.Algorithms;
import frc.robot.utilities.MathUtilities;
import frc.robot.utilities.MovingAverageFilter;
import frc.robot.utilities.SwerveKinematics;
import frc.robot.utilities.swerveLimiter.SwerveLimiter;

public class SwerveDrive extends SwerveDriveBase {
    public static enum DriveMode {
        ThrowerOriented, PickupOriented, FieldOriented;
    }

    private DriveMode driveMode = DriveMode.ThrowerOriented;
    private static SwerveDriveBase instance = null;
    private SwerveKinematics<Constants.SwerveDrive.MountingLocations> kinematics;
    private Map<Constants.SwerveDrive.MountingLocations, SwerveModule> modules = new HashMap<>();
    private SwerveLimiter.RotationDirectionCorrectorGetter<Constants.SwerveDrive.MountingLocations> directionCorectorGetter;
    private ChassisSpeeds currentChassisSpeeds = new ChassisSpeeds();
    private double speedFactor = Constants.SwerveDrive.defaultSpeedFactor;

    private void setUpSwerveKinematics() {
        Map<Constants.SwerveDrive.MountingLocations, Translation2d> mountingPoints = Constants.SwerveDrive.swerveModuleConfigs
                .entrySet().stream().map(Algorithms.mapEntryFunction((config) -> config.mountingPoint))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        kinematics = new SwerveKinematics<Constants.SwerveDrive.MountingLocations>(mountingPoints);
    }

    private void setUpSwerveModules() {
        modules = Constants.SwerveDrive.swerveModuleConfigs.entrySet().stream()
                .map(Algorithms.mapEntryFunction(SwerveModule::new))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
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

    private double getMaxSpeed(Map<Constants.SwerveDrive.MountingLocations, SwerveModuleState> states) {
        return states.values().stream().max(Comparator.comparing((state) -> state.speedMetersPerSecond))
                .get().speedMetersPerSecond;
    }

    private Map<Constants.SwerveDrive.MountingLocations, SwerveModuleState> normalizeStates(
            Map<Constants.SwerveDrive.MountingLocations, SwerveModuleState> states) {
        if (getMaxSpeed(states) > Constants.SwerveDrive.maxSpeedOfDrive * speedFactor)
            return states.entrySet().stream()
                    .map(Algorithms.mapEntryFunction(
                            Algorithms.mapSwerveModuleStateSpeed((speed) -> speed / getMaxSpeed(states))))
                    .map(Algorithms
                            .mapEntryFunction(Algorithms.mapSwerveModuleStateSpeed((speed) -> speed * speedFactor)))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return states;
    }

    @Override
    public void drive(ChassisSpeeds requestedMovement) {
        currentChassisSpeeds = requestedMovement;
        Map<Constants.SwerveDrive.MountingLocations, SwerveModuleState> states = kinematics
                .toLabledSwerveModuleStates(currentChassisSpeeds);
        states = normalizeStates(states);
        // Map<Constants.SwerveDrive.MountingLocations, Double> rotationOfsetFactors =
        // SwerveLimiterBase
        // .getRotationOfsets(modules, states);

        states.entrySet()
                .forEach((Entry<Constants.SwerveDrive.MountingLocations, SwerveModuleState> labeledState) -> modules
                        .get(labeledState.getKey()).setDesiredState(labeledState.getValue(),
                                1.0 /* rotationOfsetFactors.get(labeledState.getKey()) */));

        if (Constants.SwerveDrive.rotateAllModulesInSameDirection)
            correctRotationDirections(Math.abs(currentChassisSpeeds.omegaRadiansPerSecond) > 0.01);

        forEachModule((module) -> module.drive(speedFactor));
    }

    private Map<Constants.SwerveDrive.MountingLocations, SwerveLimiter.ModuleRotationVectors> getModuleRotationVectorMap() {
        return modules.entrySet().stream()
                .map(Algorithms.mapEntryFunction(
                        (module) -> new SwerveLimiter.ModuleRotationVectors(module.getModuleRotation(),
                                module.getTargetVector())))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    @Override
    public void periodic() {
        forEachModule((module) -> module.updateLimiterGauseYOffset(getUpdatedYOffset()));
    }

    MovingAverageFilter yOffsetLimiter = new MovingAverageFilter(
            Constants.SwerveDrive.limiterYOffsetMovingAverageHistorySize);

    private double getUpdatedYOffset() {
        return MathUtilities.map(yOffsetLimiter.calculate(Robot.getPdp().getVoltage()),
                Constants.SwerveDrive.yOffsetMapperMinVoltage, Constants.SwerveDrive.yOffsetMapperMinVoltage,
                Constants.SwerveDrive.limiterConfig.gauseYOffset, 2);
    }

    private void correctRotationDirections(boolean isRobotRotating) {
        Map<Constants.SwerveDrive.MountingLocations, SwerveLimiter.ModuleRotationVectors> moduleRotatoinVectors = getModuleRotationVectorMap();
        Map<Constants.SwerveDrive.MountingLocations, Optional<Boolean>> corrections = directionCorectorGetter
                .getModuleRotationDirectionCorrections(moduleRotatoinVectors, isRobotRotating);
        corrections.entrySet().stream().filter((correctionEntry) -> correctionEntry.getValue().isPresent())
                .map(Algorithms.mapEntryFunction((correction) -> correction.get()))
                .filter((correctionEntry) -> correctionEntry.getValue())
                .forEach((correctionEntry) -> modules.get(correctionEntry.getKey()).invertRotationDirection());
    }

    @Override
    public void rotateAllModules(double speed) {
        forEachModule((module) -> module.rotateModule(speed));
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
        forEachModule((module) -> module.stopAllMotors());
    }

    @Override
    public boolean areAllModulesZeroed() {
        return modules.values().stream().map((module) -> module.hasEncoderBeenZeroed()).reduce(true,
                (previousZeroed, currentZeroed) -> previousZeroed && currentZeroed);
    }

    @Override
    public void initSendable(SendableBuilder builder) {

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
