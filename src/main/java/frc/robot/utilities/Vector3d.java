package frc.robot.utilities;

import frc.robot.utilities.GroveColorSensor.Color;

public class Vector3d {
    public double x;
    public double y;
    public double z;

    public Vector3d() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3d normalize() {
        x /= magnitude();
        y /= magnitude();
        z /= magnitude();
        return this;
    }

    public double dot(Vector3d vec) {
        return x * vec.x + y * vec.y + z * vec.z;
    }

    public static Vector3d fromBallColorToVector(Color color){
        return new Vector3d(color.red, color.green, color.blue);
    }
}
