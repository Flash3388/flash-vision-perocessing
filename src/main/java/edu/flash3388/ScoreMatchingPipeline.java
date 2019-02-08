package edu.flash3388;

import edu.flash3388.vision.ImageAnalyser;
import edu.flash3388.vision.cv.CvProcessing;
import edu.wpi.cscore.CvSource;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.vision.VisionPipeline;
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

public class ScoreMatchingPipeline implements VisionPipeline {

	private static final int DRAW_CIRCLE_RADIUS = 5;
	private static final double MIN_COUNTOR_SIZE = 20;
	private static final double MAX_COUNTOR_SIZE = 200;
	private static final Scalar DRAW_CIRCLE_COLOR = new Scalar(255, 0, 0);
	private static final Scalar BEST_PAIR_COLOR = new Scalar(78, 150, 200);

	private static final int MIN_HUE = 0;
	private static final int MAX_HUE = 180;
	private static final int MIN_SATURATION = 0;
	private static final int MAX_SATURATION = 57;
	private static final int MIN_VALUE = 243;
	private static final int MAX_VALUE = 255;
	
	private static final String OFFSET_ENTRY = "xoffset";

	private static final double APPROX_EPSILON = 0.1;
	private final double mRealTargetLength;

	private final NetworkTable mOutputTable;
	private final CvSource mResultOutput;
	private final CvProcessing mCvProcessing;
	private final ImageAnalyser mImageAnalyser;
	private final double mCamFieldOfViewRadians;

	public ScoreMatchingPipeline(NetworkTable outputTable, CvSource resultOutput, CvProcessing cvProcessing, ImageAnalyser imageAnalyser,
			double camFieldOfViewRadians) {
		this(outputTable, resultOutput, cvProcessing, imageAnalyser, camFieldOfViewRadians, 30);
	}

	public ScoreMatchingPipeline(NetworkTable outputTable, CvSource resultOutput, CvProcessing cvProcessing, ImageAnalyser imageAnalyser,
			double camFieldOfViewRadians, double realTargetLength) {
		mOutputTable = outputTable;
		mResultOutput = resultOutput;
		mCvProcessing = cvProcessing;
		mImageAnalyser = imageAnalyser;
		mCamFieldOfViewRadians = camFieldOfViewRadians;
		mRealTargetLength = realTargetLength;
	}

	@Override
	public void process(Mat image) {
		try {
            double imageWidth = image.width();

			mCvProcessing.rgbToHsv(image, image); // ~15 ms

			Range hue = new Range(MIN_HUE, MAX_HUE);
			Range saturation = new Range(MIN_SATURATION, MAX_SATURATION);
			Range value = new Range(MIN_VALUE, MAX_VALUE);

			mCvProcessing.filterMatColors(image, image, hue, saturation, value); // ~15 ms
			List<MatOfPoint> countours = mCvProcessing.detectContours(image); // ~10 ms
			
			List<RotatedRect> rotatedRects = getRotatedRects(countours); // ~3 ms

			List<RectPair> listRectPair = getPossiblePairs(rotatedRects); // ~1 ms
			if(listRectPair.size() > 0) {
				Collections.sort(listRectPair);
				RectPair bestPair = listRectPair.get(0);

                Mat pushImage = new Mat();

                Imgproc.cvtColor(image, pushImage, Imgproc.COLOR_GRAY2RGB);

				drawRotatedRect(pushImage, bestPair.rect1, BEST_PAIR_COLOR);
				drawRotatedRect(pushImage, bestPair.rect2, BEST_PAIR_COLOR);

				Point center = new Point((bestPair.rect2.center.x + bestPair.rect1.center.x) / 2.0,
						(bestPair.rect2.center.y + bestPair.rect1.center.y) / 2.0);

				System.out.println(String.format("best score - %f", (float) bestPair.score));

				Imgproc.circle(pushImage,
						center,
						DRAW_CIRCLE_RADIUS, new Scalar(0, 255, 0));

				double xOffSet = center.x - imageWidth * 0.5;
				mOutputTable.getEntry(OFFSET_ENTRY).setDouble(xOffSet);

                mResultOutput.putFrame(pushImage);
			}
			else {
				mOutputTable.getEntry(OFFSET_ENTRY).setDouble(0.0);

				mResultOutput.putFrame(image);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void printRectsScores(RectPair bestPair) {
		System.out.println(String.format("Angle score %f", bestPair.calcAngleScore()));
		System.out.println(String.format("Height score %f", bestPair.calcHeightScore()));
		System.out.println(String.format("Width score %f", bestPair.calcWidthScore()));
		System.out.println(String.format("Ypos score %f", bestPair.calcYPosScore()));
		System.out.println(String.format("centerhiehgt score %f removed", bestPair.calcCenterHeightScore()));
		System.out.println(String.format("width height 1 %d %d", bestPair.rect1.boundingRect().width ,bestPair.rect1.boundingRect().height));
		System.out.println(String.format("width height 1 %d %d", bestPair.rect2.boundingRect().width ,bestPair.rect2.boundingRect().height));
		System.out.println(String.format("angle 1 %f", bestPair.rect1.angle));
		System.out.println(String.format("angle 2 %f", bestPair.rect2.angle));
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
}
