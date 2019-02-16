package edu.tables;

public class TargetData {
    private Double mXOffset;
    private Double mDistance;
    private Double mAngleInDegrees;

    public TargetData(Double xOffset, Double distance, Double angleInDegrees) {
        mXOffset = xOffset;
        mDistance = distance;
        mAngleInDegrees = angleInDegrees;
    }

    Double getXOffset() {
        return mXOffset;
    }

    Double getDistance() {
        return mDistance;
    }

    Double getAngleInDegrees() {
        return mAngleInDegrees;
    }
}