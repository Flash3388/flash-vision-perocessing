package edu.tables;

public class TargetData {
    private Double mXOffset;
    private Double mDistance;

    public TargetData(Double xOffset, Double distance) {
        mXOffset = xOffset;
        mDistance = distance;
    }

    Double getXOffset() {
        return mXOffset;
    }

    Double getDistance() {
        return mDistance;
    }
}