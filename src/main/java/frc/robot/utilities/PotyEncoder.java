package frc.robot.utilities;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;

public class PotyEncoder {
    private TalonSRX motoControllerRefrence;
    private boolean inverted = false;
    private static final int maxTicks = 1023;
    private static final int minTicks = 0;
    private double minOut = 0.0;
    private double maxOut = 1.0;

    private static double map(double input, double input_start, double input_end, double output_start,
            double output_end) {
        return output_start + ((output_end - output_start) / (input_end - input_start)) * (input - input_start);
    }

    public PotyEncoder(TalonSRX motoController) {
        motoControllerRefrence = motoController;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public void setOutputRange(double min, double max) {
        minOut = min;
        maxOut = max;
    }

    public double getPos() {
        int rawPos = motoControllerRefrence.getSensorCollection().getAnalogIn();
        if (!inverted)
            return maxOut - map(rawPos, minTicks, maxTicks, minOut, maxOut);
        return map(rawPos, minTicks, maxTicks, minOut, maxOut);
    }
}