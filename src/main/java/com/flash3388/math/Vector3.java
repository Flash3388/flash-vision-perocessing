package com.flash3388.math;

import java.util.Objects;

public class Vector3 {

    private final double mX;
    private final double mY;
    private final double mZ;

    public Vector3(double x, double y, double z) {
        mX = x;
        mY = y;
        mZ = z;
    }

    public Vector3() {
        this(0.0, 0.0, 0.0);
    }

    public static Vector3 polarRadians(double magnitude, double azimuth, double inclination) {
        double x = magnitude * Math.sin(inclination) * Math.cos(azimuth);
        double y = magnitude * Math.sin(inclination) * Math.sin(azimuth);
        double z = magnitude * Math.cos(inclination);
        return new Vector3(x, y, z);
    }

    public static Vector3 polar(double magnitude, double azimuth, double inclination) {
        return polarRadians(magnitude, Math.toRadians(azimuth), Math.toRadians(inclination));
    }

    public double x() {
        return mX;
    }

    public double y() {
        return mY;
    }

    public double z() {
        return mZ;
    }

    public double magnitude() {
        return Math.sqrt(mX * mX + mY * mY + mZ * mZ);
    }

    public double azimuthRadians() {
        return Math.atan2(mY, mX);
    }

    public double azimuth() {
        return Math.toDegrees(azimuthRadians());
    }

    public double inclinationRadians() {
        double angle = Math.acos(mZ / magnitude());
        return (mZ < 0) ? -angle : angle;
    }

    public double inclination() {
        return Math.toDegrees(inclinationRadians());
    }

    public double max() {
        return Math.max(mX, Math.max(mY, mZ));
    }

    public double min() {
        return Math.min(mX, Math.max(mY, mZ));
    }

    public double sum() {
        return mX + mY + mZ;
    }

    public Vector3 abs() {
        return new Vector3(Math.abs(mX), Math.abs(mY), Math.abs(mZ));
    }

    public Vector3 singular() {
        return new Vector3(Math.signum(mX), Math.signum(mY), Math.signum(mZ));
    }

    public Vector3 normalized() {
        double magnitude = magnitude();
        return new Vector3(mX / magnitude, mY / magnitude, mZ / magnitude);
    }

    public Vector3 copy() {
        return new Vector3(mX, mY, mZ);
    }

    public Vector3 add(Vector3 vector) {
        return new Vector3(mX + vector.mX, mY + vector.mY, mZ + vector.mZ);
    }

    public Vector3 add(double scalar) {
        return new Vector3(mX + scalar, mY + scalar, mZ + scalar);
    }

    public Vector3 sub(Vector3 vector) {
        return new Vector3(mX - vector.mX, mY - vector.mY, mZ - vector.mZ);
    }

    public Vector3 sub(double scalar) {
        return new Vector3(mX - scalar, mY - scalar, mZ - scalar);
    }

    public Vector3 multiply(Vector3 vector) {
        return new Vector3(mX * vector.mX, mY * vector.mY, mZ * vector.mZ);
    }

    public Vector3 multiply(double scalar) {
        return new Vector3(mX * scalar, mY * scalar, mZ * scalar);
    }

    public Vector3 div(Vector3 vector) {
        return new Vector3(mX / vector.mX, mY / vector.mY, mZ / vector.mZ);
    }

    public Vector3 div(double scalar) {
        return new Vector3(mX / scalar, mY / scalar, mZ / scalar);
    }

    public double dot(Vector3 vector) {
        return mX * vector.mX + mY * vector.mY + mZ * vector.mZ;
    }

    public Vector3 cross(Vector3 vector) {
        return new Vector3(mY * vector.mZ - mZ * vector.mY, mZ * vector.mX - mX * vector.mZ, mX * vector.mY - mY * vector.mX);
    }

    public double angleTo(Vector3 other) {
        return Math.acos(dot(other) / (magnitude() + other.magnitude()));
    }

    public boolean equals(Vector3 other) {
        return other != null && mX == other.mX && mY == other.mY && mZ == other.mZ;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Vector3 && equals((Vector3)obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mX, mY, mZ);
    }

    @Override
    public String toString() {
        return String.format("(%f, %f, %f)", mX, mY, mZ);
    }
}
