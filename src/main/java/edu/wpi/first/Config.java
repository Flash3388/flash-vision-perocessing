package edu.wpi.first;

import com.flash3388.vision.template.TemplateMatchingMethod;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class Config {

    private final int mTeam;
    private final NtMode mNtMode;
    private final List<CameraConfig> mCameraConfigs;
    private final TemplateMatchingMethod mTemplateMatchingMethod;
    private final File mVisionTemplate;
    private final double mTemplateMatchingScaleFactor;

    public Config(int team, NtMode ntMode, List<CameraConfig> cameraConfigs, TemplateMatchingMethod templateMatchingMethod, File visionTemplate, double templateMatchingScaleFactor) {
        mTeam = team;
        mNtMode = ntMode;
        mCameraConfigs = Collections.unmodifiableList(cameraConfigs);
        mTemplateMatchingMethod = templateMatchingMethod;
        mVisionTemplate = visionTemplate;
        mTemplateMatchingScaleFactor = templateMatchingScaleFactor;
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

    public TemplateMatchingMethod getTemplateMatchingMethod() {
        return mTemplateMatchingMethod;
    }

    public File getVisionTemplate() {
        return mVisionTemplate;
    }

    public double getTemplateMatchingScaleFactor() {
        return mTemplateMatchingScaleFactor;
    }
}
