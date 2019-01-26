package edu.flash3388.vision;

import edu.flash3388.math.Vector3;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

public class ImageAnalyser {

    public double measureDistance(double imageDimension, double contourDimension, double actualDimension, double angleOfViewRad) {
        return (actualDimension * imageDimension / (2 * contourDimension * Math.tan(angleOfViewRad)));
    }

    public double measureDistanceByWidth(Mat image, MatOfPoint contour, double actualWidth, double angleViewRad) {
        return measureDistance(image.width(), contour.width(), actualWidth, angleViewRad);
    }

    public double measureDistanceByHeight(Mat image, MatOfPoint contour, double actualHeight, double angleViewRad) {
        return measureDistance(image.height(), contour.height(), actualHeight, angleViewRad);
    }

    public double calculateHorizontalOffsetRadians(Mat image, Point targetPoint, double fovRadians){
        // Compute focal length in pixels from FOV
        double centerX = image.height() * 0.5;
        double centerY = image.width() * 0.5;
        double focalLength = centerX / Math.tan(0.5 * fovRadians);
        // Vectors subtending image center and pixel from optical center
        // in camera coordinates.
        Vector3 center = new Vector3(0, 0, focalLength);
        Vector3 pixel = new Vector3(targetPoint.x - centerX, targetPoint.y - centerY, focalLength);

        // angle between vector (0, 0, f) and pixel
        return center.angleTo(pixel);
    }

    public double calculateHorizontalOffsetDegrees(Mat image, Point targetPoint, double fovDegrees){
        // Compute focal length in pixels from FOV
        return Math.toDegrees(calculateHorizontalOffsetRadians(image, targetPoint, Math.toRadians(fovDegrees)));
    }
}
