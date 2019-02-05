package edu.flash3388;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import edu.flash3388.vision.ImageAnalyser;
import edu.flash3388.vision.cv.CvProcessing;
import edu.flash3388.vision.template.ScaledTemplateMatchingResult;
import edu.flash3388.vision.template.TemplateMatcher;
import edu.flash3388.vision.template.TemplateMatchingException;
import edu.wpi.cscore.CvSource;
import edu.wpi.first.vision.VisionPipeline;

import edu.flash3388.RectsPair;

public class ScoreMatchingPipeline implements VisionPipeline {

	private static final int DRAW_CIRCLE_RADIUS = 5;
	private static final Scalar DRAW_CIRCLE_COLOR = new Scalar(255, 0, 0);

	private static final double APPROX_EPSILON = 0.1;
	private final CvSource mResultOutput;
	private final CvProcessing mCvProcessing;
	private final ImageAnalyser mImageAnalyser;
	private final double mCamFieldOfViewRadians;

	private boolean write = false;

	
	public ScoreMatchingPipeline(CvSource resultOutput, CvProcessing cvProcessing, ImageAnalyser imageAnalyser,
			double camFieldOfViewRadians) {
		mResultOutput = resultOutput;
		mCvProcessing = cvProcessing;
		mImageAnalyser = imageAnalyser;
		mCamFieldOfViewRadians = camFieldOfViewRadians;
	}

	@Override
	public void process(Mat image) {
		try {
			// will use this to perform vision processing, so that the original image
			// remains intact to draw info on it
			Mat hsvImage = new Mat();

			/*
			 * Convert the image to HSV color scheme
			 */
			mCvProcessing.rgbToHsv(image, hsvImage);

			/*
			 * These values represent the color filtering range. You may edit them through
			 * network tables.
			 */
			Range hue = new Range(0, 180);
			Range saturation = new Range(0, 255);
			Range value = new Range(220, 255);

			/*
			 * Filter the image by color range
			 */
			mCvProcessing.filterMatColors(hsvImage, hsvImage, hue, saturation, value);
			List<MatOfPoint> contours = mCvProcessing.detectContours(hsvImage);
						
			List<RotatedRect> rects = new ArrayList<RotatedRect>();
			Mat pushImage = new Mat();
			Imgproc.cvtColor(hsvImage, pushImage, Imgproc.COLOR_GRAY2RGB);
			for (MatOfPoint c : contours) {
					
				MatOfPoint2f cnt2f = new MatOfPoint2f(c.toArray());
			
				RotatedRect rect = Imgproc.minAreaRect(cnt2f);
				if(rect.boundingRect().area() > 10.0)
				{
					drawRotatedRect(pushImage, rect);	
					if(rect.size.width < rect.size.height)
						rect.angle += 180; 
					else
						rect.angle += 90;
	
					rects.add(rect);
				}
			}
			
			List<RectsPair> pairs = new ArrayList<RectsPair>();
			for(int i = 1; i < rects.size(); i++)
				for(int j = 0; j < i; j++){
					RotatedRect rect1 = rects.get(i);
					RotatedRect rect2 = rects.get(j);
					pairs.add(new RectsPair(rect1, rect2));
				}
			
			System.out.println(pairs.size());
			RectsPair bestPair = null;
			for(RectsPair pair : pairs)
				if(bestPair == null || bestPair.score > pair.score)
					bestPair = pair;
			if(bestPair != null)
			{
				System.out.println(String.format("best score - %f", (float)bestPair.score));
				Imgproc.circle(pushImage, new Point((bestPair.rect2.center.x + bestPair.rect1.center.x)/2.0,(bestPair.rect2.center.y + bestPair.rect1.center.y)/2.0), DRAW_CIRCLE_RADIUS, new Scalar(0, 255, 0));
			}

			mResultOutput.putFrame(pushImage);
			
			// this is the width of the template used in real life in CM
			// double realObjectWidthCm = 15.0;
			// double distanceToTargetCm = mImageAnalyser.measureDistance(image.width(),
			// image.width(), realObjectWidthCm, mCamFieldOfViewRadians);

			// double degressToTarget =
			// mImageAnalyser.calculateHorizontalOffsetDegrees(image,
			// result.getCenterPoint(), Math.toDegrees(mCamFieldOfViewRadians));
		} catch (Throwable e) {
			// change this however you want
			e.printStackTrace();
		}
	}

	private void drawRotatedRect(Mat image, RotatedRect rect) {
		MatOfPoint verticies = new MatOfPoint();

		Imgproc.boxPoints(rect, verticies);

		Point p1 = new Point((int) verticies.get(0, 0)[0], (int) verticies.get(0, 1)[0]);
		Point p2 = new Point((int) verticies.get(1, 0)[0], (int) verticies.get(1, 1)[0]);
		Point p3 = new Point((int) verticies.get(2, 0)[0], (int) verticies.get(2, 1)[0]);
		Point p4 = new Point((int) verticies.get(3, 0)[0], (int) verticies.get(3, 1)[0]);

		this.drawBox(image, p1, p2, p3, p4);
	}

	private void drawBox(Mat image, Point p1, Point p2, Point p3, Point p4) {
		Imgproc.line(image, p1, p2, DRAW_CIRCLE_COLOR, 2);
		Imgproc.line(image, p2, p3, DRAW_CIRCLE_COLOR, 2);
		Imgproc.line(image, p3, p4, DRAW_CIRCLE_COLOR, 2);
		Imgproc.line(image, p4, p1, DRAW_CIRCLE_COLOR, 2);

	}

	private void drawResult(Mat image, ScaledTemplateMatchingResult result) {
		// draw a line over the center of the image
		Imgproc.line(image, new Point(image.width() * 0.5, 0.0), new Point(image.width() * 0.5, image.height()),
				DRAW_CIRCLE_COLOR);
		// draw center point circle
		Imgproc.circle(image, result.getCenterPoint(), DRAW_CIRCLE_RADIUS, DRAW_CIRCLE_COLOR);

		mResultOutput.putFrame(image);
	}

}
