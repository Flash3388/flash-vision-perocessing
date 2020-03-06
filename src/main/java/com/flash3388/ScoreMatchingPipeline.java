package com.flash3388;

import com.flash3388.vision.ColorSettings;
import com.flash3388.vision.ImageAnalyser;
import com.flash3388.vision.cv.CvProcessing;
import edu.wpi.cscore.CvSource;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.vision.VisionPipeline;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ScoreMatchingPipeline implements VisionPipeline {
	private static final double TARGET_HEIGHT_TO_WIDTH_RATIO = 42/92.0;
	private static final double REAL_TARGET_WIDTH_CM = 92;

	private static final double MIN_CONTOUR_SIZE = 1000;
	private static final double MIN_SCORE = 0.6;

	private static final int MIN_HUE = 0;
	private static final int MAX_HUE = 180;
	private static final int MIN_SATURATION = 100;
	private static final int MAX_SATURATION = 255;
	private static final int MIN_VALUE = 105;
	private static final int MAX_VALUE = 255;

	private final CvSource resultOutput;
	private final CvProcessing cvProcessing;
	private final ImageAnalyser imageAnalyser;
	private final double camFieldOfViewRadians;
	private final double targetHeightToWidthRatio;
	private final double targetRealWidth;

	private Range hue;
	private Range saturation;
	private Range value;

	private NetworkTableEntry angleEntry;
	private NetworkTableEntry distanceEntry;
	private NetworkTableEntry scoreEntry;

	private final ColorSettings colorSettings;

	public ScoreMatchingPipeline(CvSource resultOutput, CvProcessing cvProcessing, ImageAnalyser imageAnalyser,
								 double camFieldOfViewRadians, ColorSettings colorSettings) {
		this(resultOutput, cvProcessing, imageAnalyser, camFieldOfViewRadians, TARGET_HEIGHT_TO_WIDTH_RATIO, REAL_TARGET_WIDTH_CM, colorSettings);
	}

	public ScoreMatchingPipeline(CvSource resultOutput, CvProcessing cvProcessing, ImageAnalyser imageAnalyser,
								 double camFieldOfViewRadians, double targetHeightToWidthRatio, double targetRealWidth, ColorSettings colorSettings) {
		this.colorSettings = colorSettings;
		NetworkTable visionTable = NetworkTableInstance.getDefault().getTable("vision");
		angleEntry = visionTable.getEntry("angle_degrees");
		angleEntry.setDefaultDouble(0);

        distanceEntry = visionTable.getEntry("distance_cm");
        distanceEntry.setDefaultDouble(0);

		scoreEntry = visionTable.getEntry("score");
		scoreEntry.setDefaultDouble(0);

		this.resultOutput = resultOutput;
		this.cvProcessing = cvProcessing;
		this.imageAnalyser = imageAnalyser;
		this.camFieldOfViewRadians = camFieldOfViewRadians;
		this.targetHeightToWidthRatio = targetHeightToWidthRatio;
		this.targetRealWidth = targetRealWidth;

		hue = new Range(MIN_HUE, MAX_HUE);
		saturation = new Range(MIN_SATURATION, MAX_SATURATION);
		value = new Range(MIN_VALUE, MAX_VALUE);
	}

	@Override
	public void process(Mat image) {
//		hue = colorSettings.hue();
//		saturation = colorSettings.saturation();
//		value = colorSettings.value();

		try {
			double imageWidth = image.width();
			cvProcessing.rgbToHsv(image, image);
			cvProcessing.filterMatColors(image, image, hue, saturation, value);
			Optional<RatioTarget> optionalTarget = retrieveBestTarget(cvProcessing.detectContours(image));

			double distanceCm = -1;
			double angleOffsetDegrees = 0;

			if(optionalTarget.isPresent()) {
				Target target = optionalTarget.get();
				if(target.calcScore() > MIN_SCORE) {
					scoreEntry.setDouble(target.calcScore());
					Mat pushImage = new Mat();
					Imgproc.cvtColor(image, pushImage, Imgproc.COLOR_GRAY2RGB);
					target.draw(pushImage);
					resultOutput.putFrame(pushImage);

					distanceCm = calcDistanceCM(target.width(), imageWidth);
					angleOffsetDegrees = calcAngleOffsetDegrees(target.centerX(), imageWidth);
				}
			}

			distanceEntry.setDouble(distanceCm);
			angleEntry.setDouble(angleOffsetDegrees);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private Optional<RatioTarget> retrieveBestTarget(List<MatOfPoint> contours) {
		return rectifyContours(contours).stream()
				.filter(rect -> rect.area() > MIN_CONTOUR_SIZE)
				.map(rect -> new RatioTarget(rect, targetHeightToWidthRatio))
				.max(Comparator.comparingDouble(RatioTarget::calcScore));
	}

	private List<Rect> rectifyContours(List<MatOfPoint> contours) {
		return contours.stream()
				.map(Imgproc::boundingRect)
				.collect(Collectors.toList());
	}

	private double calcDistanceCM(double width, double imageWidth) {
		return imageAnalyser.measureDistance(imageWidth, width,
				targetRealWidth, camFieldOfViewRadians);
	}

	private double calcAngleOffsetDegrees(double centerX, double imageWidth) {
		double xOffset = centerX - imageWidth * 0.5;
		double focalLengthPixel = imageWidth * 0.5 / Math.tan(Math.toDegrees(camFieldOfViewRadians) * 0.5 * Math.PI/180);
		return Math.toDegrees(Math.atan(xOffset / focalLengthPixel));
	}
}
