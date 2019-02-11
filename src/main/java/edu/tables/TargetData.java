package edu.tables;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class TargetData {
    private final static String TARGET_DATA_TABLE = "target_data_table";
    private final static String X_OFFSET_KEY = "x_offset_key";
    private final static String VISION_DISTANCE_KEY = "vision_distance_key";

    private NetworkTable mTargetDataTable;

    public TargetData() {
        mTargetDataTable = NetworkTableInstance.getDefault().getTable(TARGET_DATA_TABLE);
    }

    public void setXOffset(double xOffset) {
        mTargetDataTable.getEntry(X_OFFSET_KEY).setDouble(xOffset);
    }

    public double getXOffset(double defaultValue) {
        return mTargetDataTable.getEntry(VISION_DISTANCE_KEY).getDouble(defaultValue);
    }

    public void setVisionDistance(double visionDistance) {
        mTargetDataTable.getEntry(VISION_DISTANCE_KEY).setDouble(visionDistance);
    }

    public double getVisionDistance(double defaultValue) {
        return mTargetDataTable.getEntry(VISION_DISTANCE_KEY).getDouble(defaultValue);
    }
    

} 