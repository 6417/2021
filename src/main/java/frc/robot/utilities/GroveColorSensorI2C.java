package frc.robot.utilities;

import java.nio.ByteBuffer;

import edu.wpi.first.wpilibj.I2C;

public class GroveColorSensorI2C extends I2C {
    public static final byte COMMAND_BIT = (byte) 0x80;

    public static enum Register {
        ENABLE ((byte) 0x00),
        ENABLE_AIEN((byte) 0x10),    /* RGBC Interrupt Enable */
        ENABLE_WEN((byte) 0x08),    /* Wait enable - Writing 1 activates the wait timer */
        ENABLE_AEN((byte) 0x02),    /* RGBC Enable - Writing 1 actives the ADC, 0 disables it */
        ENABLE_PON((byte) 0x01),    /* Power on - Writing 1 activates the internal oscillator, 0 disables it */
        ATIME((byte) 0x01),    /* Integration time */
        WTIME((byte) 0x03),    /* Wait time (if TCS34725_ENABLE_WEN is asserted) */
        WTIME_2_4MS((byte) 0xFF),    /* WLONG0 = 2.4ms   WLONG1 = 0.029s */
        WTIME_204MS((byte) 0xAB),    /* WLONG0 = 204ms   WLONG1 = 2.45s  */
        WTIME_614MS((byte) 0x00),    /* WLONG0 = 614ms   WLONG1 = 7.4s   */
        AILTL((byte) 0x04),    /* Clear channel lower interrupt threshold */
        AILTH((byte) 0x05),
        AIHTL((byte) 0x06),    /* Clear channel upper interrupt threshold */
        AIHTH((byte) 0x07),
        CLEAR_INTERRUPT((byte) 0x66),
        PERS((byte) 0x0C),    /* Persistence register - basic SW filtering mechanism for interrupts */
        PERS_NONE((byte) 0b0000),  /* Every RGBC cycle generates an interrupt                                */
        PERS_1_CYCLE((byte) 0b0001),  /* 1 clean channel value outside threshold range generates an interrupt   */
        PERS_2_CYCLE((byte) 0b0010),  /* 2 clean channel values outside threshold range generates an interrupt  */
        PERS_3_CYCLE((byte) 0b0011),  /* 3 clean channel values outside threshold range generates an interrupt  */
        PERS_5_CYCLE((byte) 0b0100),  /* 5 clean channel values outside threshold range generates an interrupt  */
        PERS_10_CYCLE((byte) 0b0101),  /* 10 clean channel values outside threshold range generates an interrupt */
        PERS_15_CYCLE((byte) 0b0110),  /* 15 clean channel values outside threshold range generates an interrupt */
        PERS_20_CYCLE((byte) 0b0111),  /* 20 clean channel values outside threshold range generates an interrupt */
        PERS_25_CYCLE((byte) 0b1000),  /* 25 clean channel values outside threshold range generates an interrupt */
        PERS_30_CYCLE((byte) 0b1001),  /* 30 clean channel values outside threshold range generates an interrupt */
        PERS_35_CYCLE((byte) 0b1010),  /* 35 clean channel values outside threshold range generates an interrupt */
        PERS_40_CYCLE((byte) 0b1011),  /* 40 clean channel values outside threshold range generates an interrupt */
        PERS_45_CYCLE((byte) 0b1100),  /* 45 clean channel values outside threshold range generates an interrupt */
        PERS_50_CYCLE((byte) 0b1101),  /* 50 clean channel values outside threshold range generates an interrupt */
        PERS_55_CYCLE((byte) 0b1110),  /* 55 clean channel values outside threshold range generates an interrupt */
        PERS_60_CYCLE((byte) 0b1111),  /* 60 clean channel values outside threshold range generates an interrupt */
        CONFIG((byte) 0x0D),
        CONFIG_WLONG((byte) 0x02),    /* Choose between short and long (12x) wait times via TCS34725_WTIME */
        CONTROL((byte) 0x0F),    /* Set the gain level for the sensor */
        ID((byte) 0x12),    /* 0x44 = TCS34721/TCS34725, 0x4D = TCS34723/TCS34727 */
        STATUS((byte) 0x13),
        STATUS_AINT((byte) 0x10),    /* RGBC Clean channel interrupt */
        STATUS_AVALID((byte) 0x01),    /* Indicates that the RGBC channels have completed an integration cycle */
        CDATAL((byte) 0x14),    /* Clear channel data */
        CDATAH((byte) 0x15),
        RDATAL((byte) 0x16),    /* Red channel data */
        RDATAH((byte) 0x17),
        GDATAL((byte) 0x18),    /* Green channel data */
        GDATAH((byte) 0x19),
        BDATAL((byte) 0x1A),    /* Blue channel data */
        BDATAH((byte) 0x1B); 

        public final byte address;

        private Register(byte address) {
            this.address = address;
        }

        public static Register valueOf(byte address) {
            for (Register e : values()) {
                if (e.address == address) {
                    return e;
                }
            }
            return null;
        }
    }

    public static enum IntegrationTime

    {
        _2_4MS ((byte) 0xFF),   /**<  2.4ms - 1 cycle    - Max Count: 1024  */
        _24MS ((byte) 0xF6),   /**<  24ms  - 10 cycles  - Max Count: 10240 */
        _50MS ((byte) 0xEB),   /**<  50ms  - 20 cycles  - Max Count: 20480 */
        _101MS ((byte) 0xD5),   /**<  101ms - 42 cycles  - Max Count: 43008 */
        _154MS ((byte) 0xC0),   /**<  154ms - 64 cycles  - Max Count: 65535 */
        _700MS ((byte) 0x00);   /**<  700ms - 256 cycles - Max Count: 65535 */
        public final byte address;

        private IntegrationTime(byte address) {
            this.address = address;
        }

        public static IntegrationTime valueOf(byte address) {
            for (IntegrationTime e : values()) {
                if (e.address == address) {
                    return e;
                }
            }
            return null;
        }
    }

    public static enum Gain {
        X1 ((byte) 0x00), /** < No gain */
        X4 ((byte) 0x01), /** < 4x gain */
        X16 ((byte) 0x02), /** < 16x gain */
        X60 ((byte) 0x03); /** < 60x gain */

        public final byte address;

        private Gain(byte address) {
            this.address = address;
        }

        public static Gain valueOf(byte address) {
            for (Gain e : values()) {
                if (e.address == address) {
                    return e;
                }
            }
            return null;
        }
    }
        
    public static class Exception extends java.lang.Exception {
        public Exception(String msg) {
            super(msg);
        }

        public static class WriteFailed extends Exception {
            public WriteFailed(String msg) {
                super(msg);
            }
        }

        public static class ReadFailed extends Exception {
            public ReadFailed(String msg) {
                super(msg);
            }
        }
    }
    
    public static final int address = 0x29;
    private I2C.Port port;

    public GroveColorSensorI2C(I2C.Port port) {
        super(port, address);
        this.port = port;
    }
    
    private void directWrite(ByteBuffer data) throws Exception.WriteFailed {
        if (super.writeBulk(data, data.capacity())) 
            throw new Exception.WriteFailed("Failed to write to color sensor with port: " + port.toString());
    }

    public void write(Register reg, ByteBuffer data) throws Exception.WriteFailed {
        ByteBuffer sendBuffer = ByteBuffer.allocate(1 + data.capacity());
        sendBuffer.put(reg.address);
        sendBuffer.put(data.array());
        write(sendBuffer);
    }

    public void write(Register reg, byte data) throws Exception.WriteFailed {
        write(reg, ByteBuffer.wrap(new byte[] { data }));
    }

    public void write(ByteBuffer data) throws Exception.WriteFailed {
        data.position(0);
        data.put((byte) (COMMAND_BIT | data.get(0)));
        directWrite(data);
    }

    public void write(byte data) throws Exception.WriteFailed {
        write(ByteBuffer.wrap(new byte[] { data }));
    }

    public void write(Register reg) throws Exception.WriteFailed {
        write(ByteBuffer.wrap(new byte[] { reg.address }));
    }

    public ByteBuffer read(Register reg, int count) throws Exception {
        write(reg);
        ByteBuffer buffer = ByteBuffer.allocate(count);
        if (super.readOnly(buffer, count))
            throw new Exception.ReadFailed("Failed to read from color sensor with port: " + port.toString());
        return buffer;
    }
}