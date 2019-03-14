package edu.wpi.first;

import edu.flash3388.vision.template.TemplateMatchingMethod;

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
    
    private final int mMinRed;
    private final int mMaxRed;
    private final int mMinGreen;
    private final int mMaxGreen;
    private final int mMinBlue;
    private final int mMaxBlue; 

    public Config(int team, NtMode ntMode, List<CameraConfig> cameraConfigs, TemplateMatchingMethod templateMatchingMethod,
            File visionTemplate, double templateMatchingScaleFactor, int minRed, int maxRed, int minGreen, int maxGreen,
            int minBlue, int maxBlue) {
        mTeam = team;
        mNtMode = ntMode;
        mCameraConfigs = Collections.unmodifiableList(cameraConfigs);
        mTemplateMatchingMethod = templateMatchingMethod;
        mVisionTemplate = visionTemplate;
        mTemplateMatchingScaleFactor = templateMatchingScaleFactor;

        mMinRed = minRed;
        mMaxRed = maxRed;
        mMinGreen = minGreen;
        mMaxGreen = maxGreen;
        mMinBlue = minBlue;
        mMaxBlue = maxBlue;
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

    public int getMinRed() {
        return mMinRed;
    }

    public int getMaxRed() {
        return mMaxRed;
    }

    public int getMinGreen() {
        return mMinGreen;
    }

    public int getMaxGreen() {
        return mMaxGreen;
    }
    
    public int getMinBlue() {
        return mMinBlue;
    }

    public int getMaxBlue() {
        return mMaxBlue;
    }
}
