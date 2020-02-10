package com.flash3388.vision;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import org.opencv.core.Range;

public class ColorRange {

    private final NetworkTableEntry mMin;
    private final NetworkTableEntry mMax;

    public ColorRange(NetworkTableEntry min, NetworkTableEntry max) {
        mMin = min;
        mMax = max;
    }

    public static ColorRange fromTable(NetworkTable table, String prefix, Range initialValue) {
        NetworkTableEntry min = table.getEntry(prefix.concat(".min"));
        min.setDouble(initialValue.start);

        NetworkTableEntry max = table.getEntry(prefix.concat(".max"));
        max.setDouble(initialValue.end);

        return new ColorRange(min, max);
    }

    public int min() {
        return (int) mMin.getDouble(0.0);
    }

    public int max() {
        return (int) mMax.getDouble(0.0);
    }

    public Range asRange() {
        return new Range(min(), max());
    }
}
