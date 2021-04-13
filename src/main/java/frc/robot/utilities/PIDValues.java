package frc.robot.utilities;

import java.util.Optional;

public class PIDValues implements Cloneable {
    public Optional<Double> kF = Optional.empty();
    public Optional<Integer> slotIdX = Optional.empty();
    public double kP;
    public double kI;
    public double kD;
    public double peakOutputReverse = -1;
    public double peakOutputForward = 1;
    public Optional<Double> cruiseVelocity;
    public Optional<Double> acceleration;

    public PIDValues(double kP, double kI, double kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
    }

    public PIDValues(double kP, double kI, double kD, double kF) {
        this.kP = kP;
        this.kD = kD;
        this.kI = kI;
        this.kF = Optional.of(kF);
    }

    public PIDValues(double kP, double kI, double kD, double kF, double peakOutputReverse, double peakOutputForward) {
        this.kP = kP;
        this.kD = kD;
        this.kI = kI;
        this.kF = Optional.of(kF);
        this.peakOutputReverse = peakOutputReverse;
    }

    public PIDValues(double kP, double kI, double kD, double peakOutputReverse, double peakOutputForward) {
        this.kP = kP;
        this.kD = kD;
        this.kI = kI;
        this.peakOutputReverse = peakOutputReverse;
    }

    /**
     * @param cruiseVelocity the cruiseVelocity to set
     */
    public void setCruiseVelocity(double cruiseVelocity) {
        this.cruiseVelocity = Optional.of(cruiseVelocity);
    }

    /**
     * @param acceleration the acceleration to set
     */
    public void setAcceleration(double acceleration) {
        this.acceleration = Optional.of(acceleration);
    }

    @Override
    public PIDValues clone() {
        try {
            return (PIDValues) super.clone();
        } catch (CloneNotSupportedException e) {
            PIDValues copy = new PIDValues(kP, kI, kD, peakOutputReverse, peakOutputForward);
            kF.ifPresent((Double kF) -> copy.kF = Optional.of((double) kF)); // deep copying optional
            cruiseVelocity.ifPresent((Double cruiseVelocity) -> copy.cruiseVelocity = Optional.of((double) cruiseVelocity)); // deep copying optional
            acceleration.ifPresent((Double acceleration) -> copy.acceleration = Optional.of((double) acceleration)); // deep copying optional
            return copy;
        }
    }
}