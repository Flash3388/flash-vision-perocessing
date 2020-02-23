package main;

import com.flash3388.vision.ColorSettings;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.Config;
import edu.wpi.first.NtMode;
import edu.wpi.first.VisionConfig;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

import java.util.List;

public class NtControl {

    private final Config mConfig;
    private final NetworkTableInstance mNtInstance;

    public NtControl(Config config) {
        mConfig = config;
        mNtInstance = NetworkTableInstance.getDefault();
    }

    public void startNetworkTables() {
        if (mConfig.getNtMode() == NtMode.SERVER) {
            System.out.println("Setting up NetworkTables server");
            mNtInstance.startServer();
        } else {
            System.out.println("Setting up NetworkTables client for team " + mConfig.getTeamNumber());
            mNtInstance.startClientTeam(mConfig.getTeamNumber());
        }
    }

    public void initializeCameraSwitching(List<VideoSource> cameras) {
        MjpegServer cameraServer = CameraServer.getInstance().addSwitchedCamera("jetson_cam");
        NetworkTable cameraControlTable = mNtInstance.getTable("cameraCtrl");
        NetworkTableEntry cameraSwitchingEntry = cameraControlTable.getEntry("camera");

        cameraServer.setSource(cameras.get(0));
        cameraSwitchingEntry.setDouble(0);
        cameraSwitchingEntry.addListener(notification -> {
            try {
                cameraServer.setSource(cameras.get((int)notification.value.getDouble()));
            } catch (IndexOutOfBoundsException ignored) {}
        }, EntryListenerFlags.kUpdate);

    }

    public void initializeExposureControl(VideoSource camera) {
        NetworkTable cameraControlTable = mNtInstance.getTable("cameraCtrl");
        NetworkTableEntry exposureEntry = cameraControlTable.getEntry("exposure");

        camera.getProperty("exposure_auto").set(1);
        exposureEntry.setDouble(camera.getProperty("exposure_absolute").get());

        exposureEntry.addListener((notification) -> {
            camera.getProperty("exposure_absolute").set((int) notification.value.getDouble());
        }, EntryListenerFlags.kUpdate);
    }

    public ColorSettings colorSettings() {
        VisionConfig visionConfig = mConfig.getVisionConfig();
        return ColorSettings.fromTable(
                mNtInstance.getTable("colorSettings"),
                visionConfig.getHue(), visionConfig.getSaturation(), visionConfig.getValue()
        );
    }
}
