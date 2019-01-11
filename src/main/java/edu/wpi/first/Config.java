package edu.wpi.first;

import java.util.Collections;
import java.util.List;

public class Config {

    private final int mTeam;
    private final NtMode mNtMode;
    private final List<CameraConfig> mCameraConfigs;

    public Config(int team, NtMode ntMode, List<CameraConfig> cameraConfigs) {
        mTeam = team;
        mNtMode = ntMode;
        mCameraConfigs = Collections.unmodifiableList(cameraConfigs);
    }

    public int getTeamNumber() {
        return mTeam;
    }

    public NtMode getNtMode() {
        return mNtMode;
    }

    public List<CameraConfig> getCameraConfigs() {
        return mCameraConfigs;
    }
}
