package frc.robot.subsystems;

import frc.robot.subsystems.Base.TankDriveBase;

public class TankDrive extends TankDriveBase {
    private static TankDriveBase instance;

    private TankDrive() {

    }

    public static TankDriveBase getInstance() {
        if (instance == null) {
            instance = new TankDrive();
        }
        return instance;
    }
}