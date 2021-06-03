package frc.robot.subsystems.swerve;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SendableRegistry;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.Constants;
import frc.robot.Constants.Drive.MountingLocations;
import frc.robot.Controller;
import frc.robot.commands.swerve.BreakCommand;
import frc.robot.commands.swerve.DefaultDriveCommand;
import frc.robot.commands.swerve.FieldOriented;
import frc.robot.commands.swerve.PickupOriented;
import frc.robot.commands.swerve.SetSpeedFactor;
import frc.robot.commands.swerve.ThrowerOriented;
import frc.robot.commands.swerve.ZeroEncoders;
import frc.robot.subsystems.Drive.DriveOrientation;
import frc.robot.subsystems.base.SwerveDriveBase;
import frc.robot.utilities.Algorithms;
import frc.robot.utilities.SwerveKinematics;
import frc.robot.utilities.swerveLimiter.SwerveLimiter;

public class SwerveDrive extends SwerveDriveBase {
    private DriveOrientation driveMode = DriveOrientation.ThrowerOriented;
    private static SwerveDriveBase instance = null;
    private SwerveKinematics<Constants.Drive.MountingLocations> kinematics;
    private Map<Constants.Drive.MountingLocations, SwerveModule> modules = new HashMap<>();
    private SwerveLimiter.RotationDirectionCorrectorGetter<Constants.Drive.MountingLocations> directionCorectorGetter;
    private ChassisSpeeds currentChassisSpeeds = new ChassisSpeeds();
    private double speedFactor = Constants.SwerveDrive.defaultSpeedFactor;

    private void setUpSwerveKinematics() {
        Map<Constants.Drive.MountingLocations, Translation2d> mountingPoints = Constants.SwerveDrive.swerveModuleConfigs
                .entrySet().stream().map(Algorithms.mapEntryFunction((config) -> config.mountingPoint))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        kinematics = new SwerveKinematics<Constants.Drive.MountingLocations>(mountingPoints);
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
                // if (!Constants.MecanumDrive.IS_ENABLED)
                //     throw new Error("Tank drive can't be enabled while swerve drive is anabled");
            } else
                instance = new SwerveDriveBase();
        return instance;
    }

    @Override
    public boolean isModuleZeroed(Constants.Drive.MountingLocations mountingLocation) {
        return modules.get(mountingLocation).hasEncoderBeenZeroed();
    }

    @Override
    public void withModule(Constants.Drive.MountingLocations mountingLocation, Consumer<SwerveModule> consumer) {
        consumer.accept(modules.get(mountingLocation));
    }

    private double getMaxSpeed(Map<Constants.Drive.MountingLocations, SwerveModuleState> states) {
        return states.values().stream().max(Comparator.comparing((state) -> state.speedMetersPerSecond))
                .get().speedMetersPerSecond;
    }

    private Map<Constants.Drive.MountingLocations, SwerveModuleState> normalizeStates(
            Map<Constants.Drive.MountingLocations, SwerveModuleState> states) {
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
        Map<Constants.Drive.MountingLocations, SwerveModuleState> states = kinematics
                .toLabledSwerveModuleStates(currentChassisSpeeds);
        states = normalizeStates(states);

        states.entrySet()
                .forEach((Entry<Constants.Drive.MountingLocations, SwerveModuleState> labeledState) -> modules
                        .get(labeledState.getKey()).setDesiredState(labeledState.getValue()));

        if (Constants.SwerveDrive.rotateAllModulesInSameDirection)
            correctRotationDirections(Math.abs(currentChassisSpeeds.omegaRadiansPerSecond) > 0.01);

        System.out.println(speedFactor);
        forEachModule((module) -> module.drive(speedFactor));
    }

    private Map<Constants.Drive.MountingLocations, SwerveLimiter.ModuleRotationVectors> getModuleRotationVectorMap() {
        return modules.entrySet().stream()
                .map(Algorithms.mapEntryFunction(
                        (module) -> new SwerveLimiter.ModuleRotationVectors(module.getModuleRotation(),
                                module.getTargetVector())))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private void correctRotationDirections(boolean isRobotRotating) {
        Map<Constants.Drive.MountingLocations, SwerveLimiter.ModuleRotationVectors> moduleRotatoinVectors = getModuleRotationVectorMap();
        Map<Constants.Drive.MountingLocations, Optional<Boolean>> corrections = directionCorectorGetter
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
    public Map<Constants.Drive.MountingLocations, Boolean> areHalSensoredOfMoudlesTriggered() {
        Map<Constants.Drive.MountingLocations, Boolean> result = new HashMap<>();
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
            Consumer<Map.Entry<Constants.Drive.MountingLocations, SwerveModule>> consumer) {
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
    public DriveOrientation getDriveMode() {
        return driveMode;
    }

    @Override
    public void setDriveMode(DriveOrientation driveMode) {
        this.driveMode = driveMode;
    }

    @Override
    public void configureButtonBindings(Joystick joystick) {
        JoystickButton zeroEncodersButton = new JoystickButton(joystick, Constants.SwerveDrive.ButtounIds.zeroEncoders);
        JoystickButton fieldOrientedButton = new JoystickButton(joystick,
                Constants.SwerveDrive.ButtounIds.fieledOriented);
        JoystickButton throwerOrientedButton = new JoystickButton(joystick,
                Constants.SwerveDrive.ButtounIds.throwerOriented);
        JoystickButton pickupOrientedButton = new JoystickButton(joystick,
                Constants.SwerveDrive.ButtounIds.pickupOriented);
        JoystickButton slowSpeedFactorButton = new JoystickButton(joystick,
                Constants.SwerveDrive.ButtounIds.slowSpeedMode);
        JoystickButton zeroNavxButton = new JoystickButton(joystick, Constants.zeroNavxButtonID);
        JoystickButton breakButton = new JoystickButton(joystick, Constants.SwerveDrive.ButtounIds.breakButton);
        JoystickButton fullSpeedButton = new JoystickButton(joystick, Constants.SwerveDrive.ButtounIds.fullSpeed);

        zeroEncodersButton.whenPressed(Controller.runCommandAndCancelWhenPressedAgain(new ZeroEncoders()));
        fieldOrientedButton.whenPressed(new FieldOriented());
        throwerOrientedButton.whenPressed(new ThrowerOriented());
        pickupOrientedButton.whenPressed(new PickupOriented());
        slowSpeedFactorButton.whenPressed(Controller
                .runCommandAndCancelWhenPressedAgain(new SetSpeedFactor(Constants.SwerveDrive.slowSpeedFactor)));
        breakButton.whileHeld(new BreakCommand());
        fullSpeedButton.whenPressed(Controller
                .runCommandAndCancelWhenPressedAgain(new SetSpeedFactor(Constants.SwerveDrive.fullSpeedFactor)));
        zeroNavxButton.whenPressed(() -> Robot.getNavx().reset());
    }
}
