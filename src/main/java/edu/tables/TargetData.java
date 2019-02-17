package edu.tables;

public class TargetData {
    private double mDistance;
    private double mAngleInDegrees;

    public TargetData(double distance, double angleInDegrees) {
        mDistance = distance;
        mAngleInDegrees = angleInDegrees;
    }

    double getDistance() {
        return mDistance;
    }

    double getAngleInDegrees() {
        return mAngleInDegrees;
    }
}