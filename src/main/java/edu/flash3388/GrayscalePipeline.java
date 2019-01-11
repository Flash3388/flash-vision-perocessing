package edu.flash3388;

import edu.wpi.cscore.CvSource;
import edu.wpi.first.vision.VisionPipeline;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class GrayscalePipeline implements VisionPipeline {

    private final CvSource mSource;

    public GrayscalePipeline(CvSource source) {
        mSource = source;
    }

    @Override
    public void process(Mat image) {
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        mSource.putFrame(image);
    }
}
