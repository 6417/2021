package frc.robot.utilities;

/**
 * An iterative boolean latch.
 * <p>
 * Returns true once iff the value of newValue changes from:
 * - false to true when RISING edge detection.
 * - true to false when FALLING edge detection.
 * - false to true and true to fals when BOTH edge detection.
 * 
 * The return stay false when the newValue stays stable between
 * iterations.
 */
public class LatchedBoolean {
    private boolean mLast;
    private boolean risingEdge;
    private boolean fallingEdge;

    public enum EdgeDetection {
        RISING, FALLING, BOTH
    }

    public LatchedBoolean() {
        this(EdgeDetection.RISING);
    }

    public LatchedBoolean(final LatchedBoolean.EdgeDetection edge) {
        this(false, edge);
    }

    public LatchedBoolean(boolean startValue, final LatchedBoolean.EdgeDetection edge) {
        mLast = startValue;
        switch (edge) {
        case RISING:
            risingEdge = true;
            fallingEdge = false;
            break;
        case FALLING:
            risingEdge = false;
            fallingEdge = true;
            break;
        case BOTH:
            risingEdge = true;
            fallingEdge = true;
            break;
        }
    }

    public boolean update(final boolean newValue) {
        boolean ret = false;
        if (risingEdge && newValue && !mLast) {
            ret = true;
        }
        if (fallingEdge && !newValue && mLast) {
            ret = true;
        }
        mLast = newValue;
        return ret;
    }
}