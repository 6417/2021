package frc.robot.utilities.swerveLimiter;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpiutil.math.MathUtil;
import frc.robot.utilities.Algorithms;
import frc.robot.utilities.MathUtilities;
import frc.robot.utilities.Pair;
import frc.robot.utilities.Timer;
import frc.robot.utilities.Vector2d;

public class SwerveLimiter extends SwerveLimiterBase {
    public static class Config implements Cloneable {
        public double gauseXStrechingFactor;
        public double gauseYOffset;
        public Supplier<Long> clock;
        public long defaultLoopTime;

        public Config clone() {
            try {
                return (Config) super.clone();
            } catch (CloneNotSupportedException e) {
                Config copy = new Config();
                copy.gauseXStrechingFactor = gauseXStrechingFactor;
                copy.gauseYOffset = gauseYOffset;
                copy.clock = clock;
                copy.defaultLoopTime = defaultLoopTime;
                return copy;
            }
        }
    }

    private final double gauseStrechingFactor;
    private Timer loopTimeTimer;
    private long defaultLoopTime;
    private final double gauseYOffset;

    public SwerveLimiter(Config config) {
        gauseStrechingFactor = config.gauseXStrechingFactor;
        loopTimeTimer = new Timer(config.clock);
        defaultLoopTime = config.defaultLoopTime;
        gauseYOffset = config.gauseYOffset;
    }

    /**
     * A gause function that is filiped along the x-axis and is 0 at {@link #x} = 0
     * and 1 at infinity.
     */
    private double modifiedGauseCurve(double x) {
        return -Math.exp(-Math.pow(x * gauseStrechingFactor, 2)) + gauseYOffset;
    }

    /**
     * @return The past time since this function has been called last. If it hasn't
     *         been called yet it will return {@link #defaultLoopTime}.
     */
    private long getLoopTime() {
        return loopTimeTimer.getPastTimeAndRestart().orElse(defaultLoopTime);
    }

    /**
     * @return The rotation direction of a module with the provided vectors.
     */
    private static ModuleRotationDirection getRotationDirection(ModuleRotationVectors rotationVectorPair) {
        if (rotationVectorPair.moduleRotation
                .dot(rotationVectorPair.desiredRotation) > rotationDirectionInversionTolerance)
            return ModuleRotationDirection.None;
        if (Math.signum(rotationVectorPair.moduleRotation.cross(rotationVectorPair.desiredRotation)) < 0.0)
            return ModuleRotationDirection.Counterclockwise;
        return ModuleRotationDirection.Clockwise;
    }

    /**
     * @return The rotation direction that is most occurring, if there's an equal
     *         count of both rotation directions it will return counterclockwise.
     */
    private static <MountingLocation extends Enum<MountingLocation>> ModuleRotationDirection getDesiredRotationDirection(
            Map<MountingLocation, ModuleRotationVectors> rotationVectorPairs) {
        return getRotationDirection(rotationVectorPairs.get(rotationVectorPairs.entrySet().stream()
                .map(Algorithms
                        .mapEntryFunction((vectorPair) -> vectorPair.desiredRotation.dot(vectorPair.moduleRotation)))
                .max(Comparator.comparing(Entry<MountingLocation, Double>::getValue))
                .map((dotproductEntry) -> dotproductEntry.getKey()).get()));
    }

    public static double rotationDirectionInversionTolerance = 0.9;
    private static <MountingLocation extends Enum<MountingLocation>> Map<MountingLocation, Optional<Boolean>> mapWithEmptyOptionals(
            Stream<Entry<MountingLocation, ModuleRotationVectors>> stream) {
        return stream
                .map(Algorithms.<MountingLocation, ModuleRotationVectors, Optional<Boolean>>mapEntryFunction(
                        (ModuleRotationVectors moduleRotationVectors) -> Optional.empty()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    /**
     * @return A map of booleans, true means the rotation direction of the module at
     *         the mounting location of the key should be inverted. Calculated with
     *         out robot rotation.
     */
    private static <MountingLocation extends Enum<MountingLocation>> Map<MountingLocation, Optional<Boolean>> getModuleRotationDirectionCorrectionsWithOutRobotRotation(
            Map<MountingLocation, ModuleRotationVectors> rotationVectorPairs) {
        ModuleRotationDirection desiredRotationDirection = getDesiredRotationDirection(rotationVectorPairs);
        if (desiredRotationDirection == ModuleRotationDirection.None) {
            return mapWithEmptyOptionals(rotationVectorPairs.entrySet().stream());
        }

        return rotationVectorPairs.entrySet().stream().map(Algorithms
                .<MountingLocation, ModuleRotationVectors, Optional<Boolean>>mapEntryFunction((rotationVectorPair) -> {
                    if (rotationVectorPair.moduleRotation
                            .dot(rotationVectorPair.desiredRotation) > rotationDirectionInversionTolerance)
                        return Optional.empty();
                    return Optional.of(getRotationDirection(rotationVectorPair) != desiredRotationDirection);

                })).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    /**
     * @param <MountingLocation>  The enum with which the given map can be accessed
     * @param rotationVectorPairs The current rotation and desired rotation of all
     *                            modules in a map as
     *                            {@link #ModuleRotationVectors}. All vectors have
     *                            to be normalized.
     * @param isRobotRotating     Whether the robot is rotating or not
     * @return A map of booleans, true means the rotation direction of the module at
     *         the mounting location of the key should be inverted.
     */
    public static <MountingLocation extends Enum<MountingLocation>> Map<MountingLocation, Optional<Boolean>> getModuleRotaionDirectionCorrections(
            Map<MountingLocation, ModuleRotationVectors> rotationVectorPairs, boolean isRobotRotating) {
        SmartDashboard.putBoolean("isRobotRotating", isRobotRotating);
        if (isRobotRotating)
            return getModuleRotationDirectionCorrectionsWithRobotRotation(rotationVectorPairs);
        else
            return getModuleRotationDirectionCorrectionsWithOutRobotRotation(rotationVectorPairs);
    }

    /**
     * @return A map of booleans, true means the rotation direction of the module at
     *         the mounting location of the key should be inverted. Calculated with
     *         out robot rotation.
     */
    private static <MountingLocation extends Enum<MountingLocation>> Map<MountingLocation, Optional<Boolean>> getModuleRotationDirectionCorrectionsWithRobotRotation(
            Map<MountingLocation, ModuleRotationVectors> rotationVectorPairs) {
        return rotationVectorPairs.entrySet().stream().map(Algorithms
                .<MountingLocation, ModuleRotationVectors, Optional<Boolean>>mapEntryFunction((rotationVectorPair) -> {
                    if (rotationVectorPair.moduleRotation
                            .dot(rotationVectorPair.desiredRotation) > rotationDirectionInversionTolerance)
                        return Optional.empty();
                    return Optional.of(rotationVectorPair.moduleRotation.dot(rotationVectorPair.desiredRotation) < 0.0);
                })).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        // return mapWithEmptyOptionals(rotationVectorPairs.entrySet().stream());
    }

    /**
     * @param speed The current cruising velocity of the module in percent
     * @return The maximal dot product that the module is allowed to rotate with
     *         it's velocity. On top of the velocity the time since this function
     *         has been called is also part of the calculation, when the past time
     *         is smaller than {@link #defaultLoopTime} the limited dot product will
     *         be smalller, if it's bigger or this function hasn't been called yet,
     *         it will just return the limited dot product with a past time of
     *         {@link #defaultLoopTime}. This is to get a consistend movement with
     *         smaller and bigger loop times.
     */
    private double getLimitedDotProduct(double speed) {
        double resultOfModifiedGauseCurve = modifiedGauseCurve(speed);
        return MathUtilities.map(MathUtil.clamp(resultOfModifiedGauseCurve / (getLoopTime() / defaultLoopTime), 0.0,
                resultOfModifiedGauseCurve > 1.0 ? 1.0 : resultOfModifiedGauseCurve), 0.0, 1.0, -1.0, 1.0);
    }

    /**
     * @param solutions The two vectors to be checked.
     * @return The vector of {@link #solutions} that is nearest to the
     *         {@link #moduleRotation}.
     */
    private Vector2d getLimitedVectorCloserToModuleRotation(Pair<Vector2d, Vector2d> solutions, Vector2d moduleRotation,
            Vector2d actualTargetVector) {
        if (solutions.first.dot(actualTargetVector) > solutions.second.dot(actualTargetVector))
            return solutions.first;
        return solutions.second;
    }

    /**
     * @param solutions The two vectors to be checked.
     * @return The vector of {@link #solutions} that is nearest to the
     *         {@link #moduleRotation}. In case the {@link #actualTargetVector} is
     *         eaven closer to the module rotation it will return the
     *         {@link #actualTargetVector}.
     */
    private Vector2d getBestSolutionOfInverseDotProduct(Pair<Vector2d, Vector2d> solutions, Vector2d moduleRotation,
            Vector2d actualTargetVector) {
        Vector2d bestSoution = getLimitedVectorCloserToModuleRotation(solutions, moduleRotation, actualTargetVector);
        if (Math.abs(moduleRotation.dot(actualTargetVector)) > Math.abs(moduleRotation.dot(bestSoution)))
            return actualTargetVector;
        return bestSoution;
    }

    /**
     * @param vec                  The rotation vector of the state the length is
     *                             not used, if the length is 0 the angle will be 0.
     * @param speedMetersPerSecond The speed which the state will have.
     * @return A new {@link #SwerveModuleState} with an angle of {@link #vec} and a
     *         speed of {@link #speedMetersPerSecond}.
     */
    private SwerveModuleState vectorToSwerveModuleState(Vector2d vec, double speedMetersPerSecond) {
        return new SwerveModuleState(speedMetersPerSecond, new Rotation2d(vec.toRadians()));
    }

    /**
     * @param desiredState          The target state.
     * @param currentModuleRotation The current rotation of the module as normalized
     *                              vector.
     * @param moduleSpeed           The current cruising velocity of the module in
     *                              percent.
     * @return A limited swerve module state based on the velocity.
     */
    @Override
    public SwerveModuleState limitState(SwerveModuleState desiredState, Vector2d currentModuleRotation,
            double moduleSpeed) {
        Vector2d moduleRotation = currentModuleRotation.clone();
        Vector2d targetVector = Vector2d.fromRad(desiredState.angle.getRadians());
        double limitedDotProduct = getLimitedDotProduct(Math.abs(moduleSpeed));
        Pair<Vector2d, Vector2d> limitedTargetVectors = moduleRotation.normalize().inverseDot(limitedDotProduct);

        return vectorToSwerveModuleState(
                getBestSolutionOfInverseDotProduct(limitedTargetVectors, moduleRotation, targetVector),
                desiredState.speedMetersPerSecond);
    }
}