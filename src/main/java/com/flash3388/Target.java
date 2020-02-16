package com.flash3388;

import org.opencv.core.Mat;

public interface Target {
    double centerX();
    double width();
    double calcScore();
    void draw(Mat img);
}
