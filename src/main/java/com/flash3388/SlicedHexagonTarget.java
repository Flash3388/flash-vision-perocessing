package com.flash3388;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;

public class SlicedHexagonTarget implements Target {
    private final MatOfPoint countour;

    public SlicedHexagonTarget(MatOfPoint countour) {
        this.countour = countour;;
    }

    @Override
    public double centerX() {
        return 0;
    }

    @Override
    public double width() {
        return 0;
    }

    @Override
    public double calcScore() {
        return 0;
    }

    private MatOfPoint shape(MatOfPoint countour) {
        MatOfPoint2f countour2f = new MatOfPoint2f(countour);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        double approxDistance = Imgproc.arcLength(countour2f, true) * 0.005;
        Imgproc.approxPolyDP(countour2f, approxCurve, approxDistance, true);

        return new MatOfPoint(approxCurve);
    }
}
