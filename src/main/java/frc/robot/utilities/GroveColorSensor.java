package frc.robot.utilities;

import java.nio.ByteBuffer;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C;

public class GroveColorSensor {
    public static class RawColor {
        public int red;
        public int green;
        public int blue;
        public int clear; // sum of red, green and blue

        public RawColor(int red, int green, int blue, int clear) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.clear = clear;
        }

        public RawColor() {
            red = 0;
            green = 0;
            blue = 0;
            clear = 0;
        }

        public String toString() {
            return String.format("Red: %d, Green: %d, Blue: %d, Clear: %d", red, green, blue, clear);
        }
    }

    public static class Color {
        public int red;
        public int green;
        public int blue;

        public Color(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public Color() {
            red = 0;
            green = 0;
            blue = 0;
        }

        public String toString() {
            return String.format("Red: %d, Green: %d, Blue: %d", red, green, blue);
        }
    }

    private interface WriteAction {
        void run() throws GroveColorSensorI2C.Exception;
    }

    private interface ReadAction {
        ByteBuffer run() throws GroveColorSensorI2C.Exception;
    }

    private void colorSensorWriteActionNoExcept(WriteAction action) {
        try {
            action.run();
        } catch (GroveColorSensorI2C.Exception e) {
            DriverStation.getInstance().reportError(e.getMessage(), true);
        }
    }

    private ByteBuffer colorSensorReadActionNoExcept(ReadAction action, ByteBuffer defaultReturn) {
        try {
            return action.run();
        } catch (GroveColorSensorI2C.Exception e) {
            DriverStation.getInstance().reportError(e.getMessage(), true);
        }
        return defaultReturn;
    }

    public GroveColorSensorI2C i2c;

    /**
     * enables color sensor. Is automaticly called by constructor.
     */
    public void enable() {
        colorSensorWriteActionNoExcept(
                () -> i2c.write(GroveColorSensorI2C.Register.ENABLE, GroveColorSensorI2C.Register.ENABLE_PON.address));
        colorSensorWriteActionNoExcept(() -> i2c.write(GroveColorSensorI2C.Register.ENABLE,
                (byte) (GroveColorSensorI2C.Register.ENABLE_PON.address
                        | GroveColorSensorI2C.Register.ENABLE_AEN.address)));
    }

    public void disable() {
        byte reg = colorSensorReadActionNoExcept(() -> i2c.read(GroveColorSensorI2C.Register.ENABLE, 1),
                ByteBuffer.wrap(new byte[] { 0 })).get(0);
        colorSensorWriteActionNoExcept(() -> i2c.write(GroveColorSensorI2C.Register.ENABLE,
                (byte) (reg & ~(GroveColorSensorI2C.Register.ENABLE_PON.address
                        | GroveColorSensorI2C.Register.ENABLE_AEN.address))));
    }

    private void setInegrationTime() {
        colorSensorWriteActionNoExcept(() -> i2c.write(GroveColorSensorI2C.Register.ATIME, integrationTime.address));
    }

    private void setGain() {
        colorSensorWriteActionNoExcept(() -> i2c.write(GroveColorSensorI2C.Register.CONTROL, gain.address));
    }

    private GroveColorSensorI2C.IntegrationTime integrationTime;
    private GroveColorSensorI2C.Gain gain;

    public GroveColorSensor(I2C.Port port, GroveColorSensorI2C.IntegrationTime integrationTime,
            GroveColorSensorI2C.Gain gain) {
        i2c = new GroveColorSensorI2C(port);
        this.integrationTime = integrationTime;
        this.gain = gain;
        setInegrationTime();
        setGain();
        enable();
    }

    public Color readRGB() {
        RawColor rawColor = getRawColor();
        Color ret = new Color();
        if (rawColor.clear > 0) {
            ret.red = 255 * rawColor.red / rawColor.clear;
            ret.green = 255 * rawColor.green / rawColor.clear;
            ret.blue = 255 * rawColor.blue / rawColor.clear;
        }
        return ret;
    }

    public void setInterrupt(boolean i) {
        byte reg = colorSensorReadActionNoExcept(() -> i2c.read(GroveColorSensorI2C.Register.ENABLE, 1),
                ByteBuffer.wrap(new byte[] { 0 })).get(0);
        if (i)
            reg |= GroveColorSensorI2C.Register.ENABLE_AIEN.address;
        else
            reg &= GroveColorSensorI2C.Register.ENABLE_AIEN.address;
        byte[] wrapper = { reg }; // wrapp reg so it can be used in lambda below
        colorSensorWriteActionNoExcept(() -> i2c.write(GroveColorSensorI2C.Register.ENABLE, wrapper[0]));
    }

    public void clearInterrupt() {
        colorSensorWriteActionNoExcept(() -> i2c.write(GroveColorSensorI2C.Register.CLEAR_INTERRUPT));
    }

    public RawColor getRawColor() {
        RawColor recieved = new RawColor();
        recieved.clear = colorSensorReadActionNoExcept(() -> i2c.read(GroveColorSensorI2C.Register.CDATAL, 2),
                ByteBuffer.wrap(new byte[] { 0, 0 })).getShort(0);
        recieved.red = colorSensorReadActionNoExcept(() -> i2c.read(GroveColorSensorI2C.Register.RDATAL, 2),
                ByteBuffer.wrap(new byte[] { 0, 0 })).getShort(0);
        recieved.green = colorSensorReadActionNoExcept(() -> i2c.read(GroveColorSensorI2C.Register.GDATAL, 2),
                ByteBuffer.wrap(new byte[] { 0, 0 })).getShort(0);
        recieved.blue = colorSensorReadActionNoExcept(() -> i2c.read(GroveColorSensorI2C.Register.BDATAL, 2),
                ByteBuffer.wrap(new byte[] { 0, 0 })).getShort(0);
        return recieved;
    }
}