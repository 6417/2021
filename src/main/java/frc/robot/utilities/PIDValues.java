package frc.robot.utilities;

import java.util.Optional;

public class PIDValues implements Cloneable {
    public Optional<Double> kF = Optional.empty();
    public double kP;
    public double kI;
    public double kD;

    public PIDValues(double kP, double kI, double kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
    }

    public PIDValues(double kP, double kI, double kD, double kF) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
        this.kF = Optional.of(kF);
    }

    @Override
    public PIDValues clone() {
        try {
            return (PIDValues) super.clone();
        } catch (CloneNotSupportedException e) {
            PIDValues copy = new PIDValues(kP, kI, kD);
            kF.ifPresent((Double kF) -> copy.kF = Optional.of((double) kF)); // deep copying optional
            return copy;
        }
    }
}