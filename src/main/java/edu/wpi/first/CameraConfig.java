package edu.wpi.first;

import com.google.gson.JsonObject;

public class CameraConfig {

    private final String mName;
    private final String mPath;
    private final JsonObject mJsonData;
    private final double mCameraFieldOfViewRadians;

    public CameraConfig(String name, String path, JsonObject jsonData, double cameraFieldOfViewRadians) {
        mName = name;
        mPath = path;
        mJsonData = jsonData;
        mCameraFieldOfViewRadians = cameraFieldOfViewRadians;
    }

    public String getName() {
        return mName;
    }

    public String getPath() {
        return mPath;
    }

    public JsonObject getJsonData() {
        return mJsonData;
    }

    public double getCameraFieldOfViewRadians() {
        return mCameraFieldOfViewRadians;
    }
}
