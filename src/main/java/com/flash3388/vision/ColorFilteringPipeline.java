package com.flash3388.vision;

import com.flash3388.vision.cv.CvProcessing;
import edu.wpi.cscore.CvSource;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.vision.VisionPipeline;
import org.opencv.core.Mat;
import org.opencv.core.Range;

public class ColorFilteringPipeline implements VisionPipeline {

    private final NetworkTable mColorFilteringTable;
    private final CvSource mOutputSource;
    private final CvProcessing mCvProcessing;

    public ColorFilteringPipeline(NetworkTable colorFilteringTable, CvSource outputSource, CvProcessing cvProcessing) {
        mColorFilteringTable = colorFilteringTable;
        mOutputSource = outputSource;
        mCvProcessing = cvProcessing;

        colorFilteringTable.getEntry("hsv").setBoolean(true);
        colorFilteringTable.getEntry("range1.min").setDouble(0.0);
        colorFilteringTable.getEntry("range1.max").setDouble(180.0);
        colorFilteringTable.getEntry("range2.min").setDouble(0.0);
        colorFilteringTable.getEntry("range2.max").setDouble(255.0);
        colorFilteringTable.getEntry("range3.min").setDouble(0.0);
        colorFilteringTable.getEntry("range3.max").setDouble(255.0);
    }

    @Override
    public void process(Mat image) {
        boolean hsv = useHsv();

        if (hsv) {
            mCvProcessing.rgbToHsv(image, image);
        }

        Range range1 = getRange("range1", hsv ? 180.0 : 250.0);
        Range range2 = getRange("range2", 250.0);
        Range range3 = getRange("range3", 250.0);

        mCvProcessing.filterMatColors(image, image, range1, range2, range3);

        mOutputSource.putFrame(image);
    }

    private boolean useHsv() {
        return mColorFilteringTable.getEntry("hsv").getBoolean(true);
    }

    private Range getRange(String namePrefix, double max) {
        return new Range(
                (int) mColorFilteringTable.getEntry(String.format("%s.min", namePrefix)).getDouble(0.0),
                (int) mColorFilteringTable.getEntry(String.format("%s.max", namePrefix)).getDouble(max));
    }
}
