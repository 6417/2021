package frc.robot.utilities;

import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;

public class ShuffleBoardInformation {

    private NetworkTableEntry information;
    private double defaultV;
    private boolean defaultBoolean;

    public ShuffleBoardInformation(String tab, String name, double information) {
        this.information = Shuffleboard.getTab(tab).add(name, information).getEntry();
    }

    public ShuffleBoardInformation(String tab, String name, Boolean information) {
        this.information = Shuffleboard.getTab(tab).add(name, information).getEntry();
    }

    public ShuffleBoardInformation(String tab, String name, Sendable information) {
        Shuffleboard.getTab(tab).add(name, information);
    }

    public ShuffleBoardInformation(String tab, String name, double min, double max, double defaultV) {
        this.defaultV = defaultV;
        information = Shuffleboard.getTab(tab).add(name, defaultV).withWidget(BuiltInWidgets.kNumberSlider)
                .withProperties(Map.of("min", min, "max", max)).getEntry();
    }

    public ShuffleBoardInformation(String tab, String name, boolean information, boolean defaultV) {
        defaultBoolean = defaultV;
        this.information = Shuffleboard.getTab(tab).add(name, information).withWidget(BuiltInWidgets.kToggleButton)
                .getEntry();
    }

    public ShuffleBoardInformation(String tab, String name, DoubleSupplier supplier) {
        this.information = Shuffleboard.getTab(tab).add(name, supplier).getEntry();
    }

    public ShuffleBoardInformation(String tab, String name, BooleanSupplier supplier) {
        this.information = Shuffleboard.getTab(tab).add(name, supplier).getEntry();
    }

    public ShuffleBoardInformation(String tab, String name, String information) {
        this.information = Shuffleboard.getTab(tab).add(name, information).getEntry();
    }

    public void update(boolean value) {
        if (this.information != null) {
            information.setBoolean(value);
        }
    }

    public void update(double value) {
        if (this.information != null) {
            information.setDouble(value);
        }
    }

    public void update(String value) {
        if (this.information != null) {
            information.setString(value);
        }
    }

    public double getSliderPosition() throws IllegalArgumentException {
        if (this.information != null) {
            return information.getDouble(defaultV);
        } else {
            throw new IllegalArgumentException("This is a button or not a slider");
        }
    }

    public void setSliderPos(double value) {
        information.setDouble(value);
    }

    public boolean getButtonState() {
        return information.getBoolean(defaultBoolean);
    }

    public double getDouble() {
        return information.getDouble(0);
    }
}