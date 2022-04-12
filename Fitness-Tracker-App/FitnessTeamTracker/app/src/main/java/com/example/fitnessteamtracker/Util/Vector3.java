package com.example.fitnessteamtracker.Util;

public class Vector3 {
    public float x,y,z;

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float length() {
        return (float) Math.sqrt(x*x + y*y + z*z);
    }

    public float scalar(Vector3 other) {
        return x*other.x + y*other.y + z*other.z;
    }

    public float angleTo(Vector3 other) {
        float za = scalar(other);
        float nen = length() * other.length();
        float co = za / nen;
        return (float) Math.acos(co);
    }

    public float angleToDeg(Vector3 other) {
        return (float) Math.toDegrees(angleTo(other));
    }

    public String toString() {
        return "x: " + x + " y: " + y + " z: " + z;
    }
}
