package frc.robot.utilities;

public class Vector2d extends edu.wpi.first.wpilibj.drive.Vector2d implements Cloneable {
    public Vector2d() {
        super();
    }

    public Vector2d(double x, double y) {
        super(x, y);
    }

    /**
     * @param vec Vector with which to perform cross product.
     * @return Cross product of this vector and "vec" (this x vec).
     */
    public double cross(Vector2d vec) {
        return (x * vec.y) - (y * vec.x);
    }

    /**
     * <b>Note</b>: The vector has to be normalized.
     * 
     * @param dotProduct The value you would get by taking the dot product of the
     *                   result and this vector.
     * @return The tow vectors with a dot product of "dotProduct". Both vectors in
     *         the pair are normalized.
     * @throws ArithmeticException If the vector isn't normalized
     * @throws ArithmeticException If "dotProduct" is not a value between -1 and 1.
     */
    public Pair<Vector2d, Vector2d> inverseDot(double dotProduct) {
        // floating point comaprisen, because of the unprecice double it can happen that
        // the magnitude would be one with the exact numbers but actualy isn't.
        if (Math.abs(magnitude() - 1.0) / magnitude() > 0.000001 || magnitude() == 0.0)
            throw new ArithmeticException(
                    "The magnitude of the vector has to be 1 to perform an inverse dot product. Magnitude is: "
                            + magnitude());

        if (!(dotProduct <= 1 && dotProduct >= -1))
            throw new ArithmeticException(
                    "The dotProduct has to be between -1 and 1, since the dot product of two normalized vector can only give you a number between -1 and 1.");

        Vector2d firstSolution = new Vector2d();
        firstSolution.x = (-(Math.sqrt(x * x + y * y - dotProduct * dotProduct) * y - x * dotProduct))
                / (x * x + y * y);
        firstSolution.y = (x * Math.sqrt(x * x + y * y - dotProduct * dotProduct) + y * dotProduct) / (x * x + y * y);

        Vector2d secondSolution = new Vector2d();
        secondSolution.x = (Math.sqrt(x * x + y * y - dotProduct * dotProduct) * y + x * dotProduct) / (x * x + y * y);
        secondSolution.y = (-(x * Math.sqrt(x * x + y * y - dotProduct * dotProduct) - y * dotProduct))
                / (x * x + y * y);

        return new Pair<Vector2d, Vector2d>(firstSolution, secondSolution);
    }

    /**
     * @return A new vector wich has the same tirection as this one but with
     *         magnitude 1.
     * @throws ArithmeticException If the magnitude of the the vector is 0;
     */
    public Vector2d normalize() {
        if (magnitude() == 0)
            throw new ArithmeticException("The magnitude of the vector mustn't be 0 to normalize it.");
        return new Vector2d(x / magnitude(), y / magnitude());
    }

    /**
     * @return A new vector wich is the sum of this vecotr and "vec".
     * @param vec The vector to be added.
     */
    public Vector2d add(Vector2d vec) {
        return new Vector2d(x + vec.x, y + vec.y);
    }

    /**
     * @return A new vector which is "vec" subtracted from this vector.
     * @param vec The vector to be subtracted.
     */
    public Vector2d sub(Vector2d vec) {
        return new Vector2d(x - vec.x, y - vec.y);
    }

    /**
     * @return A new vector wich has the x and the y of this vector multiplied by
     *         "scalar".
     * @param scalar The factor to be multiplied by.
     */
    public Vector2d mult(double scalar) {
        x *= scalar;
        y *= scalar;
        return new Vector2d(x * scalar, y * scalar);
    }

    /**
     * @return A new vector wich is this vector divided by "scalar".
     * @param scalar The divisor.
     * @throws ArithmeticException If scalar is 0.
     */
    public Vector2d div(double scalar) {
        if (scalar == 0)
            throw new ArithmeticException("Can't divide vector by 0");
        return mult(1 / scalar);
    }

    /**
     * @param theta The angle in the polar space, in radians
     * @param r     The lenght of the vector both in polar and cartesian space
     * @return A new 2d vector with an angle to the x axis of "theta" and a length
     *         of "r"
     */
    public static Vector2d fromPolar(double theta, double r) {
        return new Vector2d(Math.cos(theta) * r, Math.sin(theta) * r);
    }

    /**
     * @param angle The angle of the new vector in radians
     * @return A new normalized 2d vector with an angle of "angle" to the x axis
     */
    public static Vector2d fromRad(double angle) {
        return fromPolar(angle, 1.0);
    }

    /**
     * @return A new vector wich starts where {@link #start} ends and ends wehre {@link #end} ends.
     */
    public static Vector2d fromVectorToVector(Vector2d start, Vector2d end) {
        return new Vector2d(end.x - start.x, end.y - start.y);
    }

    /**
     * @return This vector in string representation. (Vector2d: [x, y])
     */
    public String toString() {
        return String.format("Vector2d: [%f, %f]", x, y);
    }

    @Override
    public Vector2d clone() {
        return new Vector2d(x, y);
    }
}