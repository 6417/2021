package frc.robot.utilities.baseClasses;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import frc.robot.utilities.Vector2d;

public abstract class SwerveLimiterBase {
    public abstract SwerveModuleState limitState(SwerveModuleState desiredSteate, Vector2d currentModuleRotation,
            double moduleSpeed);

    public static <MountingLocation extends Enum<MountingLocation>> Map<MountingLocation, Boolean> getModuleRotaionDirectionCorrections(
            Map<MountingLocation, ModuleRotationVectors> rotationVectorPairs, boolean isRobotRotating) {
        return rotationVectorPairs.entrySet().stream()
                .map((rotatoinVectorPairEntry) -> new AbstractMap.SimpleEntry<MountingLocation, Boolean>(
                        rotatoinVectorPairEntry.getKey(), false))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public static interface RotationDirectionCorectorGetter<MountingLocation extends Enum<MountingLocation>> {
        public Map<MountingLocation, Boolean> getModuleRotationDirectionCorrections(
                Map<MountingLocation, ModuleRotationVectors> rotationDirections, boolean isRobotRotating);
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