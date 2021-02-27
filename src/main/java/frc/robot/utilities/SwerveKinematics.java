package frc.robot.utilities;

import java.util.HashMap;
import java.util.Map.Entry;

import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.kinematics.SwerveDriveKinematics;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;

public class SwerveKinematics<MountingLocaiton extends Enum<MountingLocaiton>> extends SwerveDriveKinematics {
    private HashMap<Integer, MountingLocaiton> indicies;

    public SwerveKinematics(HashMap<MountingLocaiton, Translation2d> locations) {
        super(locations.values().toArray(Translation2d[]::new));
        for (int i = 0; i < locations.size(); i++) {
            MountingLocaiton key = ((Entry<MountingLocaiton, Translation2d>) locations.entrySet().toArray()[i]).getKey();
            indicies.put(i, key);
        }
    }

    public HashMap<MountingLocaiton, SwerveModuleState> toLabledSwerveModuleStates(ChassisSpeeds chassisSpeeds) {
        SwerveModuleState[] states = super.toSwerveModuleStates(chassisSpeeds);
        HashMap<MountingLocaiton, SwerveModuleState> labeledStates = new HashMap<>();
        for (int i = 0; i < states.length; i++)
            labeledStates.put(indicies.get(i), states[i]);

        return labeledStates;
    }
}