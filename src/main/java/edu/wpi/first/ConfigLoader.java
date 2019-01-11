package edu.wpi.first;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfigLoader {

    private final File mConfigFile;

    public ConfigLoader(File configFile) {
        mConfigFile = configFile;
    }

    public Config load() throws ConfigLoadException {
        try {
            JsonElement root = readRootElement();
            if (!root.isJsonObject()) {
                throw new ConfigLoadException("root element is not a json object");
            }

            JsonObject rootObject = root.getAsJsonObject();
            return parseRootObject(rootObject);
        } catch (IOException e) {
            throw new ConfigLoadException(e);
        }
    }

    private JsonElement readRootElement() throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(mConfigFile.toPath())) {
            return new JsonParser().parse(reader);
        }
    }

    private Config parseRootObject(JsonObject rootObject) throws ConfigLoadException {
        int teamNumber = parseTeamNumber(rootObject);
        NtMode ntMode = parseNtMode(rootObject);
        List<CameraConfig> cameraConfigs = parseCameraConfigs(rootObject);

        return new Config(teamNumber, ntMode, cameraConfigs);
    }

    private int parseTeamNumber(JsonObject rootObject) throws ConfigLoadException {
        try {
            if (!rootObject.has("team")) {
                throw new ConfigLoadException("missing `team` element");
            }

            return rootObject.get("team").getAsInt();
        } catch (ClassCastException e) {
            throw new ConfigLoadException("`team` element is not an int");
        }
    }

    private NtMode parseNtMode(JsonObject rootObject) throws ConfigLoadException {
        try {
            if (!rootObject.has("ntmode")) {
                return NtMode.UNDEFINED;
            }

            String isServerStr = rootObject.get("ntmode").getAsString();

            if (isServerStr.equals("server")) {
                return NtMode.SERVER;
            }
            if (isServerStr.equals("client")) {
                return NtMode.CLIENT;
            }

            throw new ConfigLoadException("`ntmode` contains invalid value: " + isServerStr);
        } catch (ClassCastException e) {
            throw new ConfigLoadException("`ntmode` element is not a string");
        }
    }

    private List<CameraConfig> parseCameraConfigs(JsonObject rootObject) throws ConfigLoadException {
        if (!rootObject.has("cameras")) {
            throw new ConfigLoadException("missing `cameras` element");
        }

        JsonElement camerasElement = rootObject.get("cameras");
        if (!camerasElement.isJsonArray()) {
            throw new ConfigLoadException("`cameras` is not an array");
        }

        JsonArray cameras = camerasElement.getAsJsonArray();
        List<CameraConfig> cameraConfigs = new ArrayList<>();

        for (JsonElement cameraElement : cameras) {
            if (!cameraElement.isJsonObject()) {
                throw new ConfigLoadException("element in `cameras` is not an object");
            }

            JsonObject cameraObject = cameraElement.getAsJsonObject();
            cameraConfigs.add(parseCameraConfig(cameraObject));
        }

        return cameraConfigs;
    }

    private CameraConfig parseCameraConfig(JsonObject cameraRoot) throws ConfigLoadException {
        try {
            if (!cameraRoot.has("name")) {
                throw new ConfigLoadException("camera element missing `name`");
            }
            if (!cameraRoot.has("path")) {
                throw new ConfigLoadException("camera element missing `path`");
            }

            String name = cameraRoot.get("name").getAsString();
            String path = cameraRoot.get("path").getAsString();

            return new CameraConfig(name, path, cameraRoot);
        } catch (ClassCastException | IllegalStateException e) {
            throw new ConfigLoadException("camera config element is not of wanted type", e);
        }
    }
}
