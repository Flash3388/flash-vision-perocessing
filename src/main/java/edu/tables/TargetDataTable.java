package edu.tables;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableValue;
import edu.wpi.first.networktables.TableEntryListener;

public class TargetDataTable implements TableEntryListener {
    private final static String TARGET_DATA_TABLE = "target_data_table";
    private final static String X_OFFSET_KEY = "x_offset_key";
    private final static String VISION_DISTANCE_KEY = "vision_distance_key";
    private final static String ANGLE_IN_RADIANS_KEY = "angle_in_radians_key";
    private final static String DONE_KEY = "done_key";
    public final double DONE=1.0;

    private NetworkTable mTargetDataTable;
    private TargetDataListener mTargetDataListener;
    private int mTargetDoneHandler;

    public TargetDataTable() {
        mTargetDataTable = NetworkTableInstance.getDefault().getTable(TARGET_DATA_TABLE);
    }

    public void setTargetData(TargetData targetData) {
        setXOffset(targetData.getXOffset());
        setVisionDistance(targetData.getDistance());
        setTargetAngleInRadians(targetData.getAngleInRadians());
        mTargetDataTable.getEntry(DONE_KEY).setDouble(DONE);
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
   
    public void setTargetAngleInRadians(double angleInRadians) {
        mTargetDataTable.getEntry(ANGLE_IN_RADIANS_KEY).setDouble(angleInRadians);
    }

    public double getTargetAngleInRadians(double defaultValue) {
        return mTargetDataTable.getEntry(ANGLE_IN_RADIANS_KEY).getDouble(defaultValue);
    }

    public void registerTargetDataListener(TargetDataListener targetDataListener) {
        mTargetDataListener = targetDataListener;

        if (targetDataListener != null) {
            mTargetDoneHandler = mTargetDataTable.addEntryListener(DONE_KEY, this, EntryListenerFlags.kUpdate);
        }
    }

    public void unregisterTargetDataListener(TargetSelectListener targetSelectListener) {
        if (targetSelectListener != null) {
            mTargetDataTable.removeEntryListener(mTargetDoneHandler);
        }
    }

    public void valueChanged(NetworkTable table,
                      java.lang.String key,
                      NetworkTableEntry entry,
                      NetworkTableValue value,
                      int flags) {
        if (key.equals(DONE_KEY)) {
            TargetData targetData = new TargetData(getXOffset(0), getVisionDistance(0), getTargetAngleInRadians(0));
            mTargetDataListener.onTargetData(targetData);
        }
    }

} 