package main;

import edu.wpi.cscore.VideoSource;
import edu.wpi.first.Config;
import edu.wpi.first.NtMode;
import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import frc.time.JavaNanoClock;
import frc.time.sync.NtpClient;
import frc.time.sync.NtpClock;

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

    public void initializeExposureControl(VideoSource camera) {
        NetworkTable cameraControlTable = mNtInstance.getTable("cameraCtrl");
        NetworkTableEntry exposureEntry = cameraControlTable.getEntry("exposure");

        camera.getProperty("exposure_auto").set(1);
        exposureEntry.setDouble(camera.getProperty("exposure_absolute").get());

        exposureEntry.addListener((notification) -> {
            camera.getProperty("exposure_absolute").set((int) notification.value.getDouble());
        }, EntryListenerFlags.kUpdate);
    }

    public NtpClock initializeNtp() {
        NtpClock clock = new NtpClock(new JavaNanoClock());

        NetworkTable ntpTable = mNtInstance.getTable("ntp");
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

        return clock;
    }
}
