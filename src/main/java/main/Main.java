package main;
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

import edu.wpi.first.Config;
import edu.wpi.first.ConfigLoader;

import java.io.File;
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

    public static void main(String[] args) {
        String configFilePath = DEFAULT_CONFIG_FILE_PATH;
        if (args.length > 0) {
            configFilePath = args[0];
        }

        try {
            Config config = new ConfigLoader(new File(configFilePath)).load();
            CameraControl cameraControl = new CameraControl(config);
            NtControl ntControl = new NtControl(config);

            FrcVision frcVision = new FrcVision(config, cameraControl, ntControl);
            frcVision.startVision();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
