
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.flash3388.ScoreMatchingPipeline;
import edu.flash3388.vision.ColorFilteringPipeline;
import edu.flash3388.vision.ImageAnalyser;
import edu.flash3388.vision.cv.CvProcessing;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.CameraConfig;
import edu.wpi.first.Config;
import edu.wpi.first.ConfigLoader;
import edu.wpi.first.NtMode;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.vision.VisionThread;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.time.JavaNanoClock;
import frc.time.sync.NtpClient;
import frc.time.sync.NtpClock;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
/*
   JSON format:
   {
       "team": <team number>,
       "ntmode": <"client" or "server", "client" if unspecified>
       "cameras": [
           {
               "name": <camera name>
               "path": <path, e.g. "/dev/video0">
               "pixel format": <"MJPEG", "YUYV", etc>   // optional
               "width": <video mode width>              // optional
               "height": <video mode height>            // optional
               "fps": <video mode fps>                  // optional
               "brightness": <percentage brightness>    // optional
               "white balance": <"auto", "hold", value> // optional
               "exposure": <"auto", "hold", value>      // optional
               "properties": [                          // optional
                   {
                       "name": <property name>
                       "value": <property value>
                   }
               ]
           }
       ]
   }
 */

public final class Main {

    private static final String DEFAULT_CONFIG_FILE_PATH = "/boot/frc.json";

    public static void main(String... args) {
        String configFilePath = DEFAULT_CONFIG_FILE_PATH;
        if (args.length > 0) {
            configFilePath = args[0];
        }

        try {
            Config config = new ConfigLoader(new File(configFilePath)).load();

            startNetworkTables(config);

            List<VideoSource> cameras = startCameras(config);
            if (cameras.size() >= 1) {
                startVisionThread(cameras, config);
            }

            waitForever();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void startNetworkTables(Config config) {
        NetworkTableInstance ntinst = NetworkTableInstance.getDefault();
        if (config.getNtMode() == NtMode.SERVER) {
            System.out.println("Setting up NetworkTables server");
            ntinst.startServer();
        } else {
            System.out.println("Setting up NetworkTables client for team " + config.getTeamNumber());
            ntinst.startClientTeam(config.getTeamNumber());
        }
    }

    private static List<VideoSource> startCameras(Config config) {
        List<VideoSource> cameras = new ArrayList<>();
        for (CameraConfig cameraConfig : config.getCameraConfigs()) {
            cameras.add(startCamera(cameraConfig));
        }

        return cameras;
    }

    private static VideoSource startCamera(CameraConfig config) {
        System.out.println(String.format("Starting camera %s on %s", config.getName(), config.getPath()));

        VideoSource camera = CameraServer.getInstance().startAutomaticCapture(config.getName(), config.getPath());

        Gson gson = new GsonBuilder().create();
        camera.setConfigJson(gson.toJson(config.getJsonData()));

        return camera;
    }

    private static void startVisionThread(List<VideoSource> cameras, Config config) {
        NtpClock clock = new NtpClock(new JavaNanoClock());

        NetworkTable ntpTable = NetworkTableInstance.getDefault().getTable("ntp");
        NtpClient ntpClient = new NtpClient(
                ntpTable.getEntry("client"),
                ntpTable.getEntry("serverRec"),
                ntpTable.getEntry("serverSend"),
                clock);

        new Thread(()-> {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
                ntpClient.sync();
            }
        }).start();

        CvProcessing cvProcessing = new CvProcessing();
        ImageAnalyser imageAnalyser = new ImageAnalyser();
        CvSource cvSource = CameraServer.getInstance().putVideo("processed", 480, 320);
        CameraConfig camConfigs = config.getCameraConfigs().get(0);

        VisionThread visionThread = new VisionThread(cameras.get(0),
                new ScoreMatchingPipeline(cvSource, cvProcessing, imageAnalyser, camConfigs.getCameraFieldOfViewRadians()),
                 //new ColorFilteringPipeline(NetworkTableInstance.getDefault().getTable("colors"), cvSource, cvProcessing),
                 pipeline -> {
                 });

        visionThread.start();
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
