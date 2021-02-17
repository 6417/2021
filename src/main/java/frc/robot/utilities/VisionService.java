package frc.robot.utilities;

import java.util.Optional;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants;
import frc.robot.utilities.baseClasses.VisionServiceBase;

public class VisionService extends VisionServiceBase {
    private NetworkTableInstance networkTableInstance;
    private NetworkTable smartDashboard;
    private static VisionServiceBase instance;

    private VisionService() {
        super();
        networkTableInstance = NetworkTableInstance.getDefault();
        smartDashboard = networkTableInstance.getTable("SmartDashboard");
        smartDashboard.getEntry("connected").setBoolean(true);
    }

    public static VisionServiceBase getInstance() {
        if (instance == null) {
            if (Constants.Vision.IS_ENABLED)
                instance = new VisionService();
            else
                instance = new VisionServiceBase();
        }
        return instance;
    }

    public static class Values {
        public double robotAngle = -1;
        public double distance = -1;
        public boolean targetLockon = false;

        public Values() {

        }

        public Values(double robotAngle, double distance, boolean targetLockon) {
            this.robotAngle = robotAngle;
            this.distance = distance;
            this.targetLockon = targetLockon;
        }
    }

    @Override
    public Optional<Values> getValues() {
        if (smartDashboard.getEntry("currentValues").getBoolean(false)) {
            double distance = smartDashboard.getEntry("distance").getDouble(0);
            double robotAngle = smartDashboard.getEntry("robotAngle").getDouble(0);
            boolean targetLockon = smartDashboard.getEntry("targetInView").getBoolean(false);
            smartDashboard.getEntry("currentValues").setBoolean(false);
            return Optional.of(new Values(robotAngle, distance, targetLockon));
        }
        return Optional.empty();
    }

    @Override
    public void setConnectionStatus() {
        smartDashboard.getEntry("connected").setBoolean(true);
    }
}