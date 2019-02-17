package edu.tables;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableValue;
import edu.wpi.first.networktables.TableEntryListener;

public class TargetDataTable implements TableEntryListener {
    private final static String TARGET_DATA_TABLE = "target_data_table";
    private final static String VISION_DISTANCE_KEY = "vision_distance_key";
    private final static String ANGLE_IN_DEGREES_KEY = "angle_in_degrees_key";
    private final static String DONE_KEY = "done_key";
    public final double DONE=1.0;

    private NetworkTable mTargetDataTable;
    private TargetDataListener mTargetDataListener;
    private int mTargetDoneHandler;

    public TargetDataTable() {
        mTargetDataTable = NetworkTableInstance.getDefault().getTable(TARGET_DATA_TABLE);
    }

    public void setTargetData(TargetData targetData) {
        setVisionDistance(targetData.getDistance());
        setTargetAngleInDegrees(targetData.getAngleInDegrees());
        mTargetDataTable.getEntry(DONE_KEY).setDouble(DONE);
    }

    public void setVisionDistance(double visionDistance) {
        mTargetDataTable.getEntry(VISION_DISTANCE_KEY).setDouble(visionDistance);
    }

    public double getVisionDistance(double defaultValue) {
        return mTargetDataTable.getEntry(VISION_DISTANCE_KEY).getDouble(defaultValue);
    }
   
    public void setTargetAngleInDegrees(double angleInDegrees) {
        mTargetDataTable.getEntry(ANGLE_IN_DEGREES_KEY).setDouble(angleInDegrees);
    }

    public double getTargetAngleInDegrees(double defaultValue) {
        return mTargetDataTable.getEntry(ANGLE_IN_DEGREES_KEY).getDouble(defaultValue);
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
            TargetData targetData = new TargetData(getVisionDistance(0), getTargetAngleInDegrees(0));
            mTargetDataListener.onTargetData(targetData);
        }
    }

} 