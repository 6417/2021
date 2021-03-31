package frc.robot.subsystems.Swerve;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import frc.robot.Constants;
import frc.robot.Constants.SwerveDrive.MountingLocations;
import frc.robot.commands.Swerve.DefaultDriveCommand;
import frc.robot.subsystems.Base.SwerveDriveBase;
import frc.robot.utilities.Pair;
import frc.robot.utilities.SwerveKinematics;
import frc.robot.utilities.Vector2d;
import frc.robot.utilities.swerveLimiter.SwerveLimiter;

public class SwerveDrive extends SwerveDriveBase {
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

    private HashMap<Constants.SwerveDrive.MountingLocations, SwerveModuleState> normalizeStates(
            HashMap<Constants.SwerveDrive.MountingLocations, SwerveModuleState> states) {
        double maxOfCurrentSpeeds = states.values().stream()
                .max(Comparator.comparing((SwerveModuleState state) -> state.speedMetersPerSecond))
                .get().speedMetersPerSecond;
        if (maxOfCurrentSpeeds > Constants.SwerveDrive.maxSpeedOfDrive * speedFactor) {
            HashMap<Constants.SwerveDrive.MountingLocations, SwerveModuleState> normalizedStates = new HashMap<>();
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
        HashMap<Constants.SwerveDrive.MountingLocations, SwerveModuleState> states = kinematics
                .toLabledSwerveModuleStates(requestedMovement);
        states = normalizeStates(states);
        states.entrySet()
                .forEach((Entry<Constants.SwerveDrive.MountingLocations, SwerveModuleState> labeledState) -> modules
                        .get(labeledState.getKey()).setDesiredState(labeledState.getValue()));

        correctRotaionVectors();
        if (Constants.SwerveDrive.rotateAllModulesInSameDirection)
            correctRotationDirections(requestedMovement.omegaRadiansPerSecond == 0.0);

        forEachModule((module) -> module.drive());
    }

    private double getAverageDotproductOfTargetAndModuleVector() {
        return modules.values().stream().map((module) -> module.getModuleRotation().dot(module.getTargetVector()))
                .reduce((Double d1, Double d2) -> d1 + d2).get() / modules.size();
    }

    private Vector2d getBestSolutionOfCorrectedModuleVectors(SwerveModule module, Pair<Vector2d, Vector2d> solutions) {
        if (module.getModuleRotation().dot(solutions.first) > module.getModuleRotation().dot(solutions.second)
                && module.getTargetVector().dot(solutions.first) > module.getTargetVector().dot(solutions.second))
            return solutions.first;
        return solutions.second;
    }

    private void correctRotaionVectors() {
        forEachModule((module) -> {
            if (module.getModuleRotation()
                    .dot(module.getTargetVector()) > getAverageDotproductOfTargetAndModuleVector())
                return;
            Pair<Vector2d, Vector2d> solutions = module.getModuleRotation()
                    .inverseDot(getAverageDotproductOfTargetAndModuleVector());
            Vector2d correctedModuleRotationVector = getBestSolutionOfCorrectedModuleVectors(module, solutions);
            module.setDesiredState(new SwerveModuleState(module.getDesiredModuleState().speedMetersPerSecond,
                    new Rotation2d(correctedModuleRotationVector.toRadians())));
        });
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
}
