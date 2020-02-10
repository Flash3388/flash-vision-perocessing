package main;

import com.flash3388.ScoreMatchingPipeline;
import com.flash3388.vision.ColorSettings;
import com.flash3388.vision.ImageAnalyser;
import com.flash3388.vision.cv.CvProcessing;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.CameraConfig;
import edu.wpi.first.vision.VisionThread;
import frc.time.Clock;

public class VisionControl {

    private final Clock mClock;
    private final CvProcessing mCvProcessing;
    private final ImageAnalyser mImageAnalyser;
    private final CvSource mProcessedOutput;
    private final ColorSettings mColorSettings;

    public VisionControl(Clock clock, CvProcessing cvProcessing, ImageAnalyser imageAnalyser, CvSource processedOutput, ColorSettings colorSettings) {
        mClock = clock;
        mCvProcessing = cvProcessing;
        mImageAnalyser = imageAnalyser;
        mProcessedOutput = processedOutput;
        mColorSettings = colorSettings;
    }

    public void startForCamera(VideoSource camera, CameraConfig cameraConfig) {
        // use color settings param

        VisionThread visionThread = new VisionThread(camera,
                new ScoreMatchingPipeline(mProcessedOutput,
                        mCvProcessing, mImageAnalyser,
                        cameraConfig.getCameraFieldOfViewRadians(), mClock),
                pipeline -> {
                });

        visionThread.start();
    }
}
