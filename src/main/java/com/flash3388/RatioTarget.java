package com.flash3388;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

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

    @Override
    public void draw(Mat img) {
        Imgproc.rectangle(img, rect.tl(), rect.br(), new Scalar(78, 150, 200), 2);
    }

    private double rectHeightToWidthRatio() {
        return rect.height/(double)rect.width;
    }
}
