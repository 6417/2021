package frc.robot.utilities.swerveLimiter;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpiutil.math.MathUtil;
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
        public boolean centricSwerve;

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

    private double gauseStrechingFactor;
    private Timer loopTimeTimer;
    private long defaultLoopTime;
    private final boolean centricSwerve;
    private double gauseYOffset;

    public SwerveLimiter(Config config) {
        gauseStrechingFactor = config.gauseXStrechingFactor;
        loopTimeTimer = new Timer(config.clock);
        defaultLoopTime = config.defaultLoopTime;
        centricSwerve = config.centricSwerve;
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
                .dot(rotationVectorPair.desiredRotation) > rotatoinDirectionInversionTolerance)
            return ModuleRotationDirection.None;
        if (Math.signum(rotationVectorPair.moduleRotation.cross(rotationVectorPair.desiredRotation)) < 0.0)
            return ModuleRotationDirection.Counterclockwise;
        return ModuleRotationDirection.Clockwise;
    }

    /**
     * @param rotationVectorPairs The map to be checked.
     * @param direction           The element of which the count shall be returned.
     * @return The number of elements equal to {@link #element}.
     */
    private static <MountingLocation extends Enum<MountingLocation>> int getNumberOfRotationDirectionsInMap(
            Map<MountingLocation, ModuleRotationVectors> rotationVectorPairs, ModuleRotationDirection direction) {
        return (int) rotationVectorPairs.values().stream().map(SwerveLimiter::getRotationDirection)
                .filter((rotationDirection) -> direction == rotationDirection).count();
    }

    /**
     * @return The rotation direction that is most occurring, if there's an equal
     *         count of both rotation directions it will return counterclockwise.
     */
    private static <MountingLocation extends Enum<MountingLocation>> ModuleRotationDirection getDesiredRotationDirection(
            Map<MountingLocation, ModuleRotationVectors> rotationVectorPairs) {
        return getRotationDirection(rotationVectorPairs.get(rotationVectorPairs.entrySet().stream()
                .map((vectorPairEntry) -> new AbstractMap.SimpleEntry<>(vectorPairEntry.getKey(),
                        vectorPairEntry.getValue().desiredRotation.dot(vectorPairEntry.getValue().moduleRotation)))
                .max(Comparator.comparing(Entry<MountingLocation, Double>::getValue))
                .map((dotproductEntry) -> dotproductEntry.getKey()).get()));
    }

    public static double rotatoinDirectionInversionTolerance = 0.9;

    private static <MountingLocation extends Enum<MountingLocation>> Map<MountingLocation, Optional<Boolean>> mapWithEmptyOptionals(
            Stream<Entry<MountingLocation, ModuleRotationVectors>> stream) {
        return stream.map((entry) -> new AbstractMap.SimpleEntry<MountingLocation, Optional<Boolean>>(entry.getKey(),
                Optional.empty())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
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

        return rotationVectorPairs.entrySet().stream().map((rotationVectorPair) -> {
            if (rotationVectorPair.getValue().moduleRotation
                    .dot(rotationVectorPair.getValue().desiredRotation) > rotatoinDirectionInversionTolerance)
                return new AbstractMap.SimpleEntry<MountingLocation, Optional<Boolean>>(rotationVectorPair.getKey(),
                        Optional.empty());
            return new AbstractMap.SimpleEntry<MountingLocation, Optional<Boolean>>(rotationVectorPair.getKey(),
                    Optional.of(getRotationDirection(rotationVectorPair.getValue()) != desiredRotationDirection));

        }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
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
        // return rotationVectorPairs.entrySet().stream().map((rotationVectorPair) -> {
        // if (rotationVectorPair.getValue().moduleRotation
        // .dot(rotationVectorPair.getValue().desiredRotation) >
        // rotatoinDirectionInversionTolerance)
        // return new AbstractMap.SimpleEntry<MountingLocation,
        // Optional<Boolean>>(rotationVectorPair.getKey(),
        // Optional.empty());
        // return new AbstractMap.SimpleEntry<MountingLocation,
        // Optional<Boolean>>(rotationVectorPair.getKey(),
        // Optional.of(rotationVectorPair.getValue().moduleRotation
        // .dot(rotationVectorPair.getValue().desiredRotation) < 0.0));
        // }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return mapWithEmptyOptionals(rotationVectorPairs.entrySet().stream());
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
        if (centricSwerve)
            return MathUtil.clamp(resultOfModifiedGauseCurve / (getLoopTime() / defaultLoopTime), 0.0,
                    resultOfModifiedGauseCurve);
        return MathUtilities.map(MathUtil.clamp(resultOfModifiedGauseCurve / (getLoopTime() / defaultLoopTime), 0.0,
                resultOfModifiedGauseCurve), 0.0, 1.0, -1.0, 1.0);
    }

    /**
     * @param solutions The two vectors to be checked.
     * @return The vector of {@link #solutions} that is nearest to the
     *         {@link #moduleRotation}.
     */
    private Vector2d getLimitedVectorCloserToModuleRotation(Pair<Vector2d, Vector2d> solutions, Vector2d moduleRotation,
            Vector2d actualTargetVector) {
        if (centricSwerve) {
            if (solutions.first.dot(actualTargetVector) > solutions.second.dot(actualTargetVector)
                    ^ moduleRotation.dot(actualTargetVector) < 0.0)
                return solutions.first;
            return solutions.second;
        } else if (solutions.first.dot(actualTargetVector) > solutions.second.dot(actualTargetVector))
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
     * Rotates {@link #moduleRotation} by 180° if {@link #targetRotation} has an
     * angle grater than 90° to {@link #moduleRotation}, because the other way than
     * is shorter.
     */
    private void rotateModuleRotationIfNecessary(Vector2d moduleRotation, Vector2d targetRotation) {
        if (Math.signum(moduleRotation.dot(targetRotation)) < 0.0)
            moduleRotation.rotate(180);
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
     * @param rotatoinOfsetFactor   Factor to multiply the limited dotproduct, use
     *                              the getRotationOfsetFunction to compute it.
     * 
     * @return A limited swerve module state based on the velocity.
     */
    @Override
    public SwerveModuleState limitState(SwerveModuleState desiredState, Vector2d currentModuleRotation,
            double moduleSpeed, double rotatoinOfsetFactor) {
        Vector2d moduleRotation = currentModuleRotation.clone();
        Vector2d targetVector = Vector2d.fromRad(desiredState.angle.getRadians());

        if (centricSwerve)
            rotateModuleRotationIfNecessary(moduleRotation, targetVector);

        double limitedDotProduct = getLimitedDotProduct(Math.abs(moduleSpeed)) * rotatoinOfsetFactor;
        Pair<Vector2d, Vector2d> limitedTargetVectors = moduleRotation.normalize().inverseDot(limitedDotProduct);

        return vectorToSwerveModuleState(
                getBestSolutionOfInverseDotProduct(limitedTargetVectors, moduleRotation, targetVector),
                desiredState.speedMetersPerSecond);
    }
}