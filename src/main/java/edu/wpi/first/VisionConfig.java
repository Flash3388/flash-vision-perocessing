package edu.wpi.first;

import org.opencv.core.Range;

public class VisionConfig {

    private final Range mHue;
    private final Range mSaturation;
    private final Range mValue;

    public VisionConfig(Range hue, Range saturation, Range value) {
        mHue = hue;
        mSaturation = saturation;
        mValue = value;
    }

    public Range getHue() {
        return mHue;
    }

    public Range getSaturation() {
        return mSaturation;
    }

    public Range getValue() {
        return mValue;
    }
}
