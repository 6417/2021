package frc.robot.utilities;

import java.util.Optional;

public class PIDValues {
    public PIDValues(double kP, double kI, double kD){
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
    }
    public Optional<Double> kF = Optional.empty();
    public Optional<Integer> slotIdX = Optional.empty();
    public double kP;
    public double kI;
    public double kD;
}