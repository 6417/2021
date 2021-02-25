package frc.robot.utilities;

import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpiutil.math.MathUtil;

public class SwerveLimiter {
    public static class Config {

    }

    private double gauseStrechingFactor;
    private Timer loopTimeTimer;
    private long defaultLoopTime;

    public SwerveLimiter(Config config) {

    }

    /**
     * A gause function that is filiped along the x-axis and is 0 at speed = 0
     * 
     * @param speed The current cruising velocity of the module in percent
     * @return The calculated value of the gauseFunction
     */
    private double modifiedGauseCurve(double speed) {
        return -Math.exp(-(speed * speed) * gauseStrechingFactor) + 1;
    }

    /**
     * @return The past time since this function has been called last. If it hasn't
     *         been called yet it will return {@link #defaultLoopTime}
     */
    private long getLoopTime() {
        long loopTime = defaultLoopTime;
        if (loopTimeTimer.getPastTime().isPresent())
            loopTime = loopTimeTimer.getPastTimeAndRestart().get();
        else
            loopTimeTimer.start();
        return loopTime;
    }

    /**
     * @param speed The current cruising velocity of the module in percent
     * @return The maximal dot product that the module is allowed to rotate with
     *         it's velocity. On top of the velocity the time since this function
     *         has been called is also part of the calculation, when the past time
     *         is smaller than {@link #defaultLoopTime} the limited dot product will
     *         be smalller, if it's bigger or this function hasn't been called yet,
     *         it will just return the limited dot product with a past time of
     *         {@link #defaultLoopTime}. This is to get a consistend movement with
     *         smaller and bigger loop times.
     */
    private double getLimitedDotProduct(double speed) {
        double resultOfModifiedGauseCurve = modifiedGauseCurve(speed);
        return MathUtil.clamp(resultOfModifiedGauseCurve / (getLoopTime() / defaultLoopTime), 0.0,
                resultOfModifiedGauseCurve);
    }

    /**
     * @param desiredState   The target state
     * @param moduleRotation The current rotation of the module as normalized vector
     * @param moduleSpeed    The current cruising velocity of the module in percent
     * @return A limited swerve module state based on the velocity
     */
    public SwerveModuleState limitState(SwerveModuleState desiredState, Vector2d moduleRotation, double moduleSpeed) {

        return new SwerveModuleState();
    }
}