package edu.wpi.first;

import com.google.gson.JsonObject;

public class CameraConfig {

    private final String mName;
    private final String mPath;
    private final JsonObject mJsonData;

    public CameraConfig(String name, String path, JsonObject jsonData) {
        mName = name;
        mPath = path;
        mJsonData = jsonData;
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
}
