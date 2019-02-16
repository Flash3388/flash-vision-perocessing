package edu.tables;

public class TargetData {
    private Double mXOffset;
    private Double mDistance;
    private Double mAngleInRadians;

    public TargetData(Double xOffset, Double distance, Double angleInRadians) {
        mXOffset = xOffset;
        mDistance = distance;
        mAngleInRadians = angleInRadians;
    }

    Double getXOffset() {
        return mXOffset;
    }

    Double getDistance() {
        return mDistance;
    }

    Double getAngleInRadians() {
        return mAngleInRadians;
    }
}