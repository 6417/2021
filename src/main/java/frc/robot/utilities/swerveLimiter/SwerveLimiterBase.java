package frc.robot.utilities.swerveLimiter;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpiutil.math.MathUtil;
import frc.robot.subsystems.swerve.SwerveModule;
import frc.robot.utilities.MathUtilities;
import frc.robot.utilities.Vector2d;

public abstract class SwerveLimiterBase {
    public abstract SwerveModuleState limitState(SwerveModuleState desiredSteate, Vector2d currentModuleRotation,
            double moduleSpeed, double rotatoinOfsetFactor);

    /**
     * Function for {@link #RotationDirectionCorectorGetter}, which does nothing.
     */
    public static <MountingLocation extends Enum<MountingLocation>> Map<MountingLocation, Boolean> getModuleRotaionDirectionCorrections(
            Map<MountingLocation, ModuleRotationVectors> rotationVectorPairs, boolean isRobotRotating) {
        return rotationVectorPairs.entrySet().stream()
                .map((rotatoinVectorPairEntry) -> new AbstractMap.SimpleEntry<MountingLocation, Boolean>(
                        rotatoinVectorPairEntry.getKey(), false))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public static interface RotationDirectionCorrectorGetter<MountingLocation extends Enum<MountingLocation>> {
        public Map<MountingLocation, Boolean> getModuleRotationDirectionCorrections(
                Map<MountingLocation, ModuleRotationVectors> rotationDirections, boolean isRobotRotating);
    }

    public static <MountingLocation extends Enum<MountingLocation>> Map<MountingLocation, Double> getRotationOfsets(
            Map<MountingLocation, SwerveModule> modules, Map<MountingLocation, SwerveModuleState> desiredStates) {
        double minimalDotproductBetweenModuleAndTargetVector = modules.entrySet().stream()
                .map((Entry<MountingLocation, SwerveModule> moduleEntry) -> Vector2d
                        .fromRad(desiredStates.get(moduleEntry.getKey()).angle.getRadians())
                        .dot(moduleEntry.getValue().getModuleRotation()))
                .min(Comparator.naturalOrder()).get();
        Map<MountingLocation, Double> results = new HashMap<>();
        modules.entrySet()
                .forEach(
                        (Entry<MountingLocation, SwerveModule> moduleEntry) -> results.put(moduleEntry.getKey(),
                                MathUtil.clamp(MathUtilities.map(
                                        moduleEntry.getValue().getModuleRotation()
                                                .dot(Vector2d.fromRad(
                                                        desiredStates.get(moduleEntry.getKey()).angle.getRadians())),
                                        minimalDotproductBetweenModuleAndTargetVector, 1.0, -1.0, 1.0), -1.0, 1.0)));

        return results;
    }

    public static enum ModuleRotationDirection {
        Clockwise, Counterclockwise
    }

    public static class ModuleRotationVectors {
        public Vector2d moduleRotation;
        public Vector2d desiredRotation;

        public ModuleRotationVectors(Vector2d moduleRotation, Vector2d desiredRotation) {
            this.moduleRotation = moduleRotation;
            this.desiredRotation = desiredRotation;
        }
    }
}