package edu.flash3388;

import java.util.ArrayList;
import java.util.Collections;
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
	private static final Scalar BEST_PAIR_COLOR = new Scalar(78, 150, 200);

	private static final int doble= 5;

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
			//image = Imgcodecs.imread("/home/pi/templates/templ2019.jpg");
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
					drawRotatedRect(pushImage, rect, DRAW_CIRCLE_COLOR);	
					if(rect.size.width < rect.size.height)
						rect.angle += 180; 
					else
						rect.angle += 90;
	
					rects.add(rect);
				}
			}
			System.out.println(rects.size());
			
			List<RectsPair> pairs = getRectsPairs(rects);
			
			if(pairs.size() > 0)
			{
				Collections.sort(pairs);
				RectsPair bestPair = pairs.get(0);
				drawRes(pushImage, bestPair);
				
				double realObjectWidthCm = 30.0;
				double centerdist = bestPair.centerDistance();	
				double distanceToTargetCm = mImageAnalyser.measureDistance((double)image.width(),
						 centerdist, realObjectWidthCm, mCamFieldOfViewRadians);
				System.out.println(String.format("distance %f" , (float)distanceToTargetCm));
				// double degressToTarget =
				// result.getCenterPoint(), Math.toDegrees(mCamFieldOfViewRadians));

			}
			// just for debugging
			if(!write){		
				System.out.println("writing image");
				write = true;
				Imgcodecs.imwrite("/home/pi/res1.jpg", pushImage);
			}
			
			mResultOutput.putFrame(pushImage);
			
			// this is the width of the template used in real life in CM
		} catch (Throwable e) {
			// change this however you want
			e.printStackTrace();
		}
	}

	private List<RectsPair> getRectsPairs(List<RotatedRect> rects) {
		List<RectsPair> pairs = new ArrayList<RectsPair>();
		for(int i = 1; i < rects.size(); i++)
			for(int j = 0; j < i; j++){
				RotatedRect rect1 = rects.get(i);
				RotatedRect rect2 = rects.get(j);
				pairs.add(new RectsPair(rect1, rect2));
			}
		return pairs;
	}

	private void drawRes(Mat pushImage, RectsPair bestPair) {
		Imgproc.circle(pushImage,bestPair.rect1.center, DRAW_CIRCLE_RADIUS - 1, BEST_PAIR_COLOR);
		Imgproc.circle(pushImage,bestPair.rect2.center, DRAW_CIRCLE_RADIUS - 1 , BEST_PAIR_COLOR);
		System.out.println(String.format("best score - %f", (float)bestPair.score));
		Imgproc.circle(pushImage, new Point((bestPair.rect2.center.x + bestPair.rect1.center.x)/2.0,(bestPair.rect2.center.y + bestPair.rect1.center.y)/2.0), DRAW_CIRCLE_RADIUS, new Scalar(0, 255, 0));
	}

	private void drawRotatedRect(Mat image, RotatedRect rect, Scalar color) {
		MatOfPoint verticies = new MatOfPoint();

		Imgproc.boxPoints(rect, verticies);

		Point p1 = new Point((int) verticies.get(0, 0)[0], (int) verticies.get(0, 1)[0]);
		Point p2 = new Point((int) verticies.get(1, 0)[0], (int) verticies.get(1, 1)[0]);
		Point p3 = new Point((int) verticies.get(2, 0)[0], (int) verticies.get(2, 1)[0]);
		Point p4 = new Point((int) verticies.get(3, 0)[0], (int) verticies.get(3, 1)[0]);

		this.drawBox(image, p1, p2, p3, p4, color);
	}

	private void drawBox(Mat image, Point p1, Point p2, Point p3, Point p4, Scalar color) {
		Imgproc.line(image, p1, p2, color, 2);
		Imgproc.line(image, p2, p3, color, 2);
		Imgproc.line(image, p3, p4, color, 2);
		Imgproc.line(image, p4, p1, color, 2);

	}

}
