package main;

import com.flash3388.vision.ColorSettings;
import com.flash3388.vision.ImageAnalyser;
import com.flash3388.vision.cv.CvProcessing;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.Config;
import edu.wpi.first.cameraserver.CameraServer;

import java.util.List;

public class FrcVision {
    private final Config mConfig;
    private final CameraControl mCameraControl;
    private final NtControl mNtControl;

    public FrcVision(Config config, CameraControl cameraControl, NtControl ntControl) {
        mConfig = config;
        mCameraControl = cameraControl;
        mNtControl = ntControl;
    }

    public void startVision() {
        mNtControl.startNetworkTables();

        List<VideoSource> cameras = mCameraControl.startCameras();
        if (cameras.size() >= 1) {
            startVisionThread(cameras, mConfig);
            waitForever();
        } else {
            System.out.println("No cameras");
        }
    }

    private void startVisionThread(List<VideoSource> cameras, Config config) {
        VideoSource camera = cameras.get(0);
        mNtControl.initializeExposureControl(camera);

        CvSource cvSource = CameraServer.getInstance()
                .putVideo("processed", 480, 320);

        ColorSettings colorSettings = mNtControl.colorSettings();

        new VisionControl(
                new CvProcessing(), new ImageAnalyser(),
                cvSource, colorSettings)
                .startForCamera(camera, config.getCameraConfigs().get(0));
    }

    private static void waitForever() {
        for (;;) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                return;
            }
        }
    }
}
