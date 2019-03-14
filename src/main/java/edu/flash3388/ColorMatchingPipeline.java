package edu.flash3388;

import frc.tables.TargetData;
import frc.tables.TargetDataTable;
import frc.tables.TargetSelectTable;
import frc.tables.TargetSelectListener;

import edu.flash3388.vision.ImageAnalyser;
import edu.flash3388.vision.cv.CvProcessing;

import edu.wpi.cscore.CvSource;
import edu.wpi.first.Config;
import edu.wpi.first.vision.VisionPipeline;

import org.opencv.core.Core;
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

public class ColorMatchingPipeline implements VisionPipeline, TargetSelectListener {
    private static final double MIN_COUNTOR_SIZE = 25;
    private static final double MAX_COUNTOR_SIZE = 200;
    private static final double MIN_RIGHT_SCORE = 0.9;
    private static final double DISTANCE_BETWEEN_CENTERS_CM = 28.0;
    private static final double DISTANCE_BETWEEN_CENTERS_CM_REVERSED = 1 / 28.0;
    private static final Scalar[] BEST_PAIRS_COLOR = new Scalar[] { new Scalar(78, 150, 200), new Scalar(200, 120, 150),
            new Scalar(150, 23, 87) };

    private final CvSource mResultOutput;
    private final CvProcessing mCvProcessing;
    private final ImageAnalyser mImageAnalyser;

    private final Range mRedRange;
    private final Range mGreenRange;
    private final Range mBlueRange;

	private TargetDataTable mTargetDataTable;
    private TargetSelectTable mTargetSelectTable;
    private int mTargetSelectNum;
    private Boolean mSendTargetData;

    private final double mRealTargetLength;
    private final double mCamFieldOfViewRadians;

    public ColorMatchingPipeline(CvSource resultOutput, CvProcessing cvProcessing, ImageAnalyser imageAnalyser, Config config,
            double camFieldOfViewRadians, double realTargetLength) {
        mResultOutput = resultOutput;
        mCvProcessing = cvProcessing;
        mImageAnalyser = imageAnalyser;
        mCamFieldOfViewRadians = camFieldOfViewRadians;
        mRealTargetLength = realTargetLength;

        mRedRange = new Range(config.getMinRed(), config.getMaxRed());
        mGreenRange = new Range(config.getMinGreen(), config.getMaxGreen());
        mBlueRange = new Range(config.getMinBlue(), config.getMaxBlue());
    }

    public void onTargetSelectPressed(int targetNumber) {
        mTargetSelectNum = targetNumber;
        mSendTargetData = true;
    }

    public void OnNextTargetSelectPressed() {
        mSendTargetData = true;
        System.out.println("Called ");
    }

    @Override
    public void process(Mat image) {
        try {
            mCvProcessing.filterMatColors(image, image, mRedRange, mGreenRange, mBlueRange); // ~15 ms?

            List<MatOfPoint> countours = mCvProcessing.detectContours(image); // ~10 ms
            List<RotatedRect> rotatedRects = getRotatedRects(countours); // ~3 ms
            List<RectPair> listRectPair = getPossiblePairs(rotatedRects); // ~1 ms
            int amountRects = listRectPair.size();

            if (amountRects > 0) {
                Collections.sort(listRectPair);
                Mat pushImage = new Mat();

                Imgproc.cvtColor(image, pushImage, Imgproc.COLOR_GRAY2RGB);

                for (int i = 0; i < 1 && i < listRectPair.size(); i++) {
                    RectPair currPair = listRectPair.get(i);
                    if (currPair.score >= MIN_RIGHT_SCORE) {
                        char ch = (char) (i + 'A');
                        Imgproc.putText(pushImage, String.valueOf(ch), currPair.getCenter(), Core.FONT_HERSHEY_COMPLEX, 1,
                                BEST_PAIRS_COLOR[i]);
                        System.out.println(String.format("score %f char %c ", currPair.score, ch));

                        drawRotatedRect(pushImage, currPair.rect1, BEST_PAIRS_COLOR[i]);
                        drawRotatedRect(pushImage, currPair.rect2, BEST_PAIRS_COLOR[i]);
                    }
                }

                if (mSendTargetData && mTargetSelectNum < listRectPair.size()) {
                    sendTargetData(image, listRectPair.get(mTargetSelectNum), image.width());
                }

                mResultOutput.putFrame(pushImage);
            } else {
                mResultOutput.putFrame(image);
            }
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }

    private List<RotatedRect> getRotatedRects(List<MatOfPoint> contours) {
        List<RotatedRect> rects = new ArrayList<>();

        contours.stream().filter((mat) -> {
            long total = mat.total();
            return total > MIN_COUNTOR_SIZE && total < MAX_COUNTOR_SIZE;
        }).forEach((mat) -> {
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

    private List<RectPair> getPossiblePairs(List<RotatedRect> rects) {
        List<RectPair> pairs = new ArrayList<>();
        IntStream.range(0, rects.size()).forEach((i) -> IntStream.range(i + 1, rects.size()).forEach((j) -> {
            RectPair pair = new RectPair(rects.get(i), rects.get(j));
            if (pair.rect1.angle < 50.0 && pair.rect2.angle > 130.0) {
                pairs.add(pair);
            }
        }));
        return pairs;
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

    private double getDistanceCM(RectPair pair, double imageWidth) {
        return mImageAnalyser.measureDistance(imageWidth, pair.centerDistance(), mRealTargetLength,
                mCamFieldOfViewRadians);
    }

    private double getAngleDegrees(RectPair pair, double xoffset, double distance) {
        double ratio = pair.centerDistance() * DISTANCE_BETWEEN_CENTERS_CM_REVERSED;
        double angle = Math.toDegrees(Math.asin(Math.abs(xoffset / ratio) / distance));
        return angle * Math.signum(xoffset);
    }

    private void sendTargetData(Mat image, RectPair rectPair, double imageWidth) {
        System.out.println("Send ");
        Point center = rectPair.getCenter();
        double distance = getDistanceCM(rectPair, imageWidth);
        double angleInDegress = getAngleDegrees(rectPair, center.x - imageWidth * 0.5, distance);
        mTargetDataTable.setTargetData(new TargetData(distance, angleInDegress));
        mSendTargetData = false;
    }
}
