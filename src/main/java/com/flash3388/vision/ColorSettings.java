package com.flash3388.vision;

import edu.wpi.first.networktables.NetworkTable;
import org.opencv.core.Range;
import org.opencv.core.Scalar;

public class ColorSettings {

    private final ColorRange mHue;
    private final ColorRange mSaturation;
    private final ColorRange mValue;

    public ColorSettings(ColorRange hue, ColorRange saturation, ColorRange value) {
        mHue = hue;
        mSaturation = saturation;
        mValue = value;
    }

    public static ColorSettings fromTable(NetworkTable table, Range initialHue, Range initialSaturation, Range initialValue) {
        ColorRange hue = ColorRange.fromTable(table, "hue", initialHue);
        ColorRange saturation = ColorRange.fromTable(table, "saturation", initialSaturation);
        ColorRange value = ColorRange.fromTable(table, "value", initialValue);

        return new ColorSettings(hue, saturation, value);
    }

    public Range hue() {
        return mHue.asRange();
    }

    public Range saturation() {
        return mSaturation.asRange();
    }

    public Range value() {
        return mValue.asRange();
    }

    public Scalar min() {
        return new Scalar(mHue.min(), mSaturation.min(), mValue.min());
    }

    public Scalar max() {
        return new Scalar(mHue.max(), mSaturation.max(), mValue.max());
    }
}
