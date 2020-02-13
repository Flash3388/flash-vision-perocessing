package com.flash3388;

import org.opencv.core.MatOfPoint;

public class ScorableJewishHexa implements ScorableTarget{
    private final List<MatOfPoint> countours;

    public ScorableJewishHexa(List<MatOfPoint> countours) {
        this.countours = countours;

    }

    @Override
    public double score() {
        return 0;
    }
}
