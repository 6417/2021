package frc.robot.utilities;

import java.util.Optional;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import frc.robot.Constants;
import frc.robot.utilities.baseClasses.VisionServiceBase;

public class VisionService extends VisionServiceBase {
    private NetworkTableInstance networkTableInstance;
    private NetworkTable smartDashboard;
    private Values values;
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
        public boolean targetInView = false;
        public double targetAngle = -1;
        public double viewingSide = 0;
        public double stripeHeight = 0;

        public Values() {

        }

        public Values(double robotAngle, double distance, boolean targetInView, double targetAngle, double viewingSide, double stripeHeight) {
            this.robotAngle = robotAngle;
            this.distance = distance;
            this.targetInView = targetInView;
            this.targetAngle = targetAngle;
            this.viewingSide = viewingSide;
            this.stripeHeight = stripeHeight;
        }
    }

    public Optional<Values> getValuesOptional() {
        if (smartDashboard.getEntry("currentValues").getBoolean(false)) {
            if (smartDashboard.getEntry("connected").getBoolean(false) == false)
            setConnectionStatus();
            double distance = smartDashboard.getEntry("distance").getDouble(0);
            double robotAngle = smartDashboard.getEntry("robotAngle").getDouble(0);
            boolean targetInView = smartDashboard.getEntry("targetInView").getBoolean(false);
            double targetAngle = smartDashboard.getEntry("targetAngle").getDouble(0);
            double viewingSide = smartDashboard.getEntry("viewingSide").getDouble(0);
            double stripeHeight = smartDashboard.getEntry("stripeHeight").getDouble(0);
            smartDashboard.getEntry("currentValues").setBoolean(false);
            return Optional.of(new Values(robotAngle, distance, targetInView, targetAngle, viewingSide, stripeHeight));
        }
        return Optional.empty();
    }

    @Override
    public Values getValues() {
        getValuesOptional().ifPresent((a) -> this.values = a);
        return this.values;
    }



    @Override
    public void setConnectionStatus() {
        smartDashboard.getEntry("connected").setBoolean(true);
    }
}