package edu.flash3388;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import edu.flash3388.vision.ImageAnalyser;
import edu.flash3388.vision.cv.CvProcessing;
import edu.wpi.cscore.CvSource;
import edu.wpi.first.vision.VisionPipeline;

import edu.flash3388.RectPair;

public class ScoreMatchingPipeline implements VisionPipeline {

	private static final int DRAW_CIRCLE_RADIUS = 5;
	private static final double MIN_COUNTOR_SIZE = 10.0;
	private static final Scalar DRAW_CIRCLE_COLOR = new Scalar(255, 0, 0);
	private static final Scalar BEST_PAIR_COLOR = new Scalar(78, 150, 200);

	private static final int MIN_HUE = 0;
	private static final int MAX_HUE = 100;
	private static final int MIN_SATURATION = 0;
	private static final int MAX_SATURATION = 255;
	private static final int MIN_VALUE = 220;
	private static final int MAX_VALUE = 255;

	private static final double APPROX_EPSILON = 0.1;
	private final double mRealTargetLength;

	private final CvSource mResultOutput;
	private final CvProcessing mCvProcessing;
	private final ImageAnalyser mImageAnalyser;
	private final double mCamFieldOfViewRadians;

	public ScoreMatchingPipeline(CvSource resultOutput, CvProcessing cvProcessing, ImageAnalyser imageAnalyser,
			double camFieldOfViewRadians) {
		this(resultOutput, cvProcessing, imageAnalyser, camFieldOfViewRadians, 30);
	}

	public ScoreMatchingPipeline(CvSource resultOutput, CvProcessing cvProcessing, ImageAnalyser imageAnalyser,
			double camFieldOfViewRadians, double realTargetLength) {
		mResultOutput = resultOutput;
		mCvProcessing = cvProcessing;
		mImageAnalyser = imageAnalyser;
		mCamFieldOfViewRadians = camFieldOfViewRadians;
		mRealTargetLength = realTargetLength;
	}

	@Override
	public void process(Mat image) {
		try {
			//image = Imgcodecs.imread("/home/pi/templates/templ2019.jpg");

			Mat hsvImage = new Mat();
			Mat pushImage = new Mat();

			mCvProcessing.rgbToHsv(image, hsvImage);

			Range hue = new Range(MIN_HUE, MAX_HUE);
			Range saturation = new Range(MIN_SATURATION, MAX_SATURATION);
			Range value = new Range(MIN_VALUE, MAX_VALUE);


			mCvProcessing.filterMatColors(hsvImage, hsvImage, hue, saturation, value);
			Imgproc.cvtColor(hsvImage, pushImage, Imgproc.COLOR_GRAY2RGB);

			List<RectPair> bestPairs = getBestPairs(
					getPairs(getRects(mCvProcessing.detectContours(hsvImage), pushImage)), 1);

			markBestPairs(bestPairs, pushImage);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private double getDistanceCM(RectPair pair, double imageWidth) {
		return mImageAnalyser.measureDistance(imageWidth, pair.centerDistance(),
				mRealTargetLength, mCamFieldOfViewRadians);

	}
	
	private void markBestPairs(List<RectPair> bestPairs, Mat pushImage) {
		for (RectPair bestPair : bestPairs) {
			Imgproc.circle(pushImage, bestPair.rect1.center, DRAW_CIRCLE_RADIUS - 1, BEST_PAIR_COLOR);
			Imgproc.circle(pushImage, bestPair.rect2.center, DRAW_CIRCLE_RADIUS - 1, BEST_PAIR_COLOR);
			System.out.println(String.format("best score - %f", (float) bestPair.score));
			Imgproc.circle(pushImage,
					new Point((bestPair.rect2.center.x + bestPair.rect1.center.x) / 2.0,
							(bestPair.rect2.center.y + bestPair.rect1.center.y) / 2.0),
					DRAW_CIRCLE_RADIUS, new Scalar(0, 255, 0));
		}
		
		mResultOutput.putFrame(pushImage);
	}
	
	private List<RectPair> getBestPairs(List<RectPair> pairs, int numberOfPairs) {
		List<RectPair> bestPairs = new ArrayList<>();
		
		for (int i = 0; i < numberOfPairs; ++i) {
			RectPair bestPair = null;

			for (RectPair pair : pairs)
				if (bestPair == null || bestPair.score < pair.score)
					bestPair = pair;

			pairs.remove(bestPair);
			bestPairs.add(bestPair);
		}
		
		return bestPairs;
	}
	
	private List<RectPair> getPairs(List<RotatedRect> rects) {
		List<RectPair> pairs = new ArrayList<RectPair>();

		for (int i = 1; i < rects.size(); ++i)
			for (int j = 0; j < i; j++) 
				pairs.add(new RectPair(rects.get(i), rects.get(j)));
		
		return pairs;
	}
	
	private List<RotatedRect> getRects(List<MatOfPoint> contours, Mat pushImage) {
		List<RotatedRect> rects = new ArrayList<RotatedRect>();

		for (MatOfPoint c : contours) {

			MatOfPoint2f cnt2f = new MatOfPoint2f(c.toArray());

			RotatedRect rect = Imgproc.minAreaRect(cnt2f);
			if (rect.boundingRect().area() > MIN_COUNTOR_SIZE) {
				drawRotatedRect(pushImage, rect, DRAW_CIRCLE_COLOR);
				
				if (rect.size.width < rect.size.height)
					rect.angle += 180;
				else
					rect.angle += 90;

				rects.add(rect);
			}
		}

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
