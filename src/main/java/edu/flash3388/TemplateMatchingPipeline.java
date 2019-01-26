package edu.flash3388;

import edu.flash3388.vision.template.ScaledTemplateMatchingResult;
import edu.flash3388.vision.template.TemplateMatcher;
import edu.flash3388.vision.template.TemplateMatchingException;
import edu.wpi.cscore.CvSource;
import edu.wpi.first.vision.VisionPipeline;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class TemplateMatchingPipeline implements VisionPipeline {

    private static final int DRAW_CIRCLE_RADIUS = 5;
    private static final Scalar DRAW_CIRCLE_COLOR = new Scalar(255, 0, 0);

    private final TemplateMatcher mTemplateMatcher;
    private final double mInitialScaleFactor;
    private final CvSource mResultOutput;

    public TemplateMatchingPipeline(TemplateMatcher templateMatcher, double initialScaleFactor, CvSource resultOutput) {
        mTemplateMatcher = templateMatcher;
        mInitialScaleFactor = initialScaleFactor;
        mResultOutput = resultOutput;
    }


    @Override
    public void process(Mat image) {
        try {
            /*
             * matchWithScaling will attempt to match the template (or templates) with the image, returning the best match (score wise).
             * The image will be scaled to match the template, by the scale factor given, until they are the same size.
             */
            ScaledTemplateMatchingResult result = mTemplateMatcher.matchWithScaling(image, mInitialScaleFactor);
            drawResult(image, result);


            // TODO: ADD CODE TO WRITE OUTPUT INTO TABLE. MAY ALSO WRITE INFORMATION ABOUT THE IMAGE
            /*
             * result contains the following information:
             * - the center point of the matched template on the given image (x, y)
             * - scale factor used to match the image
             * - matching score, which indicates what score was received for this match (highest match out of all matches.
             */

        } catch (TemplateMatchingException e) {
            // change this however you want
            e.printStackTrace();
        }
    }

    private void drawResult(Mat image, ScaledTemplateMatchingResult result) {
        // draw a line over the center of the image
        Imgproc.line(image, new Point(image.width() * 0.5, 0.0), new Point(image.width() * 0.5, image.height()), DRAW_CIRCLE_COLOR);
        // draw center point circle
        Imgproc.circle(image, result.getCenterPoint(), DRAW_CIRCLE_RADIUS, DRAW_CIRCLE_COLOR);

        mResultOutput.putFrame(image);
    }
}
