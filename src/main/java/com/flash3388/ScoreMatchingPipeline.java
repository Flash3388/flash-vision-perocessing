package com.flash3388;

import frc.tables.TargetData;
import frc.tables.TargetDataTable;
import frc.tables.TargetSelectTable;
import frc.tables.TargetSelectListener;

import com.flash3388.vision.ImageAnalyser;
import com.flash3388.vision.cv.CvProcessing;

import edu.wpi.cscore.CvSource;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.vision.VisionPipeline;

import frc.time.Clock;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class ScoreMatchingPipeline implements VisionPipeline, TargetSelectListener  {

	private static final int DRAW_CIRCLE_RADIUS = 5;
	private static final double DISTANCE_BETWEEN_CENTERS_CM = 28.0;
	private static final double DISTANCE_BETWEEN_CENTERS_CM_REVERSED = 1/28.0;
	private static final double MIN_COUNTOR_SIZE = 10;
	private static final double MAX_COUNTOR_SIZE = 200;
	private static final Scalar DRAW_CIRCLE_COLOR = new Scalar(255, 0, 0);
	private static final Scalar BEST_PAIR_COLOR = new Scalar(78, 150, 200);
	private static final Scalar[] BEST_PAIRS_COLOR = new Scalar[] { new Scalar(78, 150, 200), new Scalar(200, 120, 150),
			new Scalar(150, 23, 87) };
	private static final double FOCAL_LENGTH_PIXEL = 680;

	private static final int MIN_HUE = 0;
	private static final int MAX_HUE = 180;
	private static final int MIN_SATURATION = 200;
	private static final int MAX_SATURATION = 255;
	private static final int MIN_VALUE = 150;
	private static final int MAX_VALUE = 255;
	private static final double MIN_RIGHT_SCORE= 0.9;

	private final double mRealTargetLength;

	private final CvSource mResultOutput;
	private final CvProcessing mCvProcessing;
	private final ImageAnalyser mImageAnalyser;
	private final double mCamFieldOfViewRadians;
	private final Clock mClock;

	private Range hue;
	private Range saturation;
	private Range value;

	private TargetDataTable mTargetDataTable;
	private TargetSelectTable mTargetSelectTable;
	private int mTargetSelectNum;
	private Boolean mSendTargetData;

	private NetworkTableEntry angleEntry;
	private NetworkTableEntry timeEntry;
	private NetworkTableEntry distanceEntry;
	private NetworkTableEntry waitEntry;
	private NetworkTableEntry runEntry;

	public ScoreMatchingPipeline(CvSource resultOutput, CvProcessing cvProcessing, ImageAnalyser imageAnalyser,
                                 double camFieldOfViewRadians, Clock clock) {
		this(resultOutput, cvProcessing, imageAnalyser, camFieldOfViewRadians, DISTANCE_BETWEEN_CENTERS_CM, clock);
		mTargetDataTable = new TargetDataTable();
		mTargetSelectTable = new TargetSelectTable();
		mTargetSelectTable.registerSelectTargetListener(this);
		mTargetSelectNum = 0;
		mSendTargetData = false;
	}

	public ScoreMatchingPipeline(CvSource resultOutput, CvProcessing cvProcessing, ImageAnalyser imageAnalyser,
                                 double camFieldOfViewRadians, double realTargetLength, Clock clock) {
        angleEntry = NetworkTableInstance.getDefault().getEntry("vision_angle");
		angleEntry.setDefaultDouble(0);

        timeEntry = NetworkTableInstance.getDefault().getEntry("vision_time");
        timeEntry.setDefaultDouble(0);

        distanceEntry = NetworkTableInstance.getDefault().getEntry("vision_distance");
        distanceEntry.setDefaultDouble(0);

        runEntry = NetworkTableInstance.getDefault().getEntry("vision_run");
        runEntry.setDefaultBoolean(false);

        waitEntry = NetworkTableInstance.getDefault().getEntry("vision_wait");
        waitEntry.setDefaultDouble(0);

		mResultOutput = resultOutput;
		mCvProcessing = cvProcessing;
		mImageAnalyser = imageAnalyser;
		mCamFieldOfViewRadians = camFieldOfViewRadians;
		mRealTargetLength = realTargetLength;
        mClock = clock;

		hue = new Range(MIN_HUE, MAX_HUE);
		saturation = new Range(MIN_SATURATION, MAX_SATURATION);
		value = new Range(MIN_VALUE, MAX_VALUE);
	}

	@Override
	public void process(Mat image) {
		try {
		    boolean isWaiting = runEntry.getBoolean(false);

			double imageWidth = image.width();

			mCvProcessing.rgbToHsv(image, image); // ~15 ms
			mCvProcessing.filterMatColors(image, image, hue, saturation, value); // ~15 ms
			List<MatOfPoint> countours = mCvProcessing.detectContours(image); // ~10 ms
//			Imgproc.approxPolyDP();
			List<RotatedRect> rotatedRects = getRotatedRects(countours); // ~3 ms
			List<RectPair> listRectPair = getPossiblePairs(rotatedRects); // ~1 ms
			int amountRects = listRectPair.size();

			if (amountRects > 0) {
				Collections.sort(listRectPair);

				Mat pushImage = new Mat();

				Imgproc.cvtColor(image, pushImage, Imgproc.COLOR_GRAY2RGB);

				drawRotatedRect(pushImage, listRectPair.get(0).rect1, BEST_PAIR_COLOR);
				drawRotatedRect(pushImage, listRectPair.get(0).rect2, BEST_PAIR_COLOR);
				mResultOutput.putFrame(pushImage);
				// sendTargetData(image, listRectPair.get(0), imageWidth);
			// 	double distance = getDistanceCM(listRectPair.get(0), imageWidth);
			// 	double xoffset = listRectPair.get(0).getCenter().x - imageWidth *0.5;
				// 	System.out.println("Mine: " +getAngleDegrees(listRectPair.get(0),xoffset,distance)+" Klein's: "+getAngle(listRectPair.get(0), FOCAL_LENGTH_PIXEL, imageWidth)+ " \n	Others: "+(Math.toDegrees(mCamFieldOfViewRadians)/imageWidth)*xoffset + " \nDistance: "+distance);

                distanceEntry.setDouble(getDistanceCM(listRectPair.get(0), imageWidth));

				angleEntry.setDouble(getAngle(listRectPair.get(0), FOCAL_LENGTH_PIXEL, imageWidth));
                timeEntry.setDouble(mClock.currentTimeMillis());

                if (isWaiting) {
                    waitEntry.setDouble(waitEntry.getDouble(0) + 1);
                }
		} else {
				mResultOutput.putFrame(image);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private double getAngleDegrees(RectPair pair, double xoffset, double distance) {
		double ratio = pair.centerDistance() * DISTANCE_BETWEEN_CENTERS_CM_REVERSED;
		double angle = Math.toDegrees(Math.asin(Math.abs(xoffset / ratio) / distance));
		return angle * Math.signum(xoffset);
	}

	private double getDistanceCM(RectPair pair, double imageWidth) {
		return mImageAnalyser.measureDistance(imageWidth, pair.centerDistance(),
				mRealTargetLength, mCamFieldOfViewRadians);
	}

	private List<RectPair> getPossiblePairs(List<RotatedRect> rects) {
		List<RectPair> pairs = new ArrayList<>();
        IntStream.range(0, rects.size())
                .forEach((i) -> IntStream.range(i + 1, rects.size())
                        .forEach((j) -> {
                            RectPair pair = new RectPair(rects.get(i), rects.get(j));
                            if(pair.rect1.angle < 50.0 && pair.rect2.angle > 130.0) {
                                pairs.add(pair);
                            }
                        }));
		return pairs;
	}
	
	private List<RotatedRect> getRotatedRects(List<MatOfPoint> contours) {
		List<RotatedRect> rects = new ArrayList<>();

        contours.stream()
                .filter((mat)-> {
                    long total = mat.total();
                    return total > MIN_COUNTOR_SIZE && total < MAX_COUNTOR_SIZE;
                }).forEach((mat)-> {
            MatOfPoint2f cnt2f = new MatOfPoint2f(mat.toArray());
            RotatedRect rect = Imgproc.minAreaRect(cnt2f);

            if (rect.size.width < rect.size.height) {
                rect.angle += 180;
            } else {
                rect.angle += 90;
            }

            rects.add(rect);
        });

        return rects;
	}

	
	private void drawRotatedRect(Mat image, RotatedRect rect, Scalar color) {
		MatOfPoint verticies = new MatOfPoint();

		Imgproc.boxPoints(rect, verticies);

		Point p1 = new Point((int) verticies.get(0, 0)[0], (int) verticies.get(0, 1)[0]);
		Point p2 = new Point((int) verticies.get(1, 0)[0], (int) verticies.get(1, 1)[0]);
		Point p3 = new Point((int) verticies.get(2, 0)[0], (int) verticies.get(2, 1)[0]);
		Point p4 = new Point((int) verticies.get(3, 0)[0], (int) verticies.get(3, 1)[0]);
		drawBox(image, p1, p2, p3, p4, color);
	}

	private void drawBox(Mat image, Point p1, Point p2, Point p3, Point p4, Scalar color) {
		Imgproc.line(image, p1, p2, color, 2);
		Imgproc.line(image, p2, p3, color, 2);
		Imgproc.line(image, p3, p4, color, 2);
		Imgproc.line(image, p4, p1, color, 2);
	}
	
	private void printRectsScores(RectPair bestPair) {
		System.out.println(String.format("Angle score %f", bestPair.calcAngleScore()));
		System.out.println(String.format("Height score %f", bestPair.calcHeightScore()));
		System.out.println(String.format("Width score %f", bestPair.calcWidthScore()));
		System.out.println(String.format("Ypos score %f", bestPair.calcYPosScore()));
		System.out.println(String.format("centerPoint center = rectPair.getCenter();hiehgt score %f removed", bestPair.calcCenterHeightScore()));
		System.out.println(String.format("width height 1 %d %d", bestPair.rect1.boundingRect().width ,bestPair.rect1.boundingRect().height));
		System.out.println(String.format("width height 1 %d %d", bestPair.rect2.boundingRect().width ,bestPair.rect2.boundingRect().height));
		System.out.println(String.format("angle 1 %f", bestPair.rect1.angle));
		System.out.println(String.format("angle 2 %f", bestPair.rect2.angle));
	}

	public void onTargetSelectPressed(int targetNumber) {
		mTargetSelectNum = targetNumber;
		mSendTargetData = true;
	}

	public void OnNextTargetSelectPressed() {
		mSendTargetData = true;
		System.out.println("Called ");
	}
	
	private void sendTargetData(Mat image, RectPair rectPair, double imageWidth) {
		System.out.println("Send ");
		double distance = getDistanceCM(rectPair, imageWidth);
		double angleInDegress = getAngle(rectPair, FOCAL_LENGTH_PIXEL, imageWidth);
		mTargetDataTable.setTargetData(new TargetData(distance, angleInDegress));
		mSendTargetData = false;
	}

	private double getAngle(RectPair rectPair, double focalLength, double imageWidth) {
		Point center = rectPair.getCenter();
		double xoffset = center.x - imageWidth * 0.5;

		return Math.toDegrees(Math.atan(xoffset / focalLength));
	}
}
