package com.flash3388;

import org.opencv.core.Rect;

public class RatioTarget implements Target {
    private final double expectedHeightToWidthRation;
    private final Rect rect;

    public RatioTarget(Rect target, double expectedHeightToWidthRation) {
        this.expectedHeightToWidthRation = expectedHeightToWidthRation;
        this.rect = target;
    }

    @Override
    public double centerX() {
        return rect.x + rect.width/2.0;
    }

    @Override
    public double width() {
        return rect.width;
    }

    @Override
    public double calcScore() {
        double actualRatio = rectHeightToWidthRatio();

        return actualRatio > expectedHeightToWidthRation ? expectedHeightToWidthRation/actualRatio : actualRatio/expectedHeightToWidthRation;
    }

    private double rectHeightToWidthRatio() {
        return rect.height/(double)rect.width;
    }
}
