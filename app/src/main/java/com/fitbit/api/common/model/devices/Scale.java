package com.fitbit.api.common.model.devices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Scale {

    private final String id;
    private final String name;
    private final String network;
    private final String battery;
    private final String lastSyncTime;
    private final String lastMeasurementTime;
    private final String lastMeasurementTimeForMe;
    private final String defaultUnit;
    private final int brightness;
    private final String color;
    private final ScaleVersion version;
    private final String firmwareVersion;

    public Scale(JSONObject json) throws JSONException {
        id = json.getString("id");
        name = json.getString("name");
        network = json.getString("network");
        battery = json.optString("battery", null);
        lastSyncTime = json.optString("lastSyncTime", null);
        lastMeasurementTime = json.optString("lastMeasurementTime", null);
        lastMeasurementTimeForMe = json.optString("lastMeasurementTimeForMe", null);
        defaultUnit =  json.getString("defaultUnit");
        brightness = json.getInt("brightness");
        color = json.optString("color", null);
        version =  ScaleVersion.valueOf(json.getString("version"));
        firmwareVersion = json.optString("firmwareVersion", null);
    }

    public static List<Scale> jsonArrayToScalesList(JSONArray array) throws JSONException {
        List<Scale> scaleList = new ArrayList<Scale>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject device = array.getJSONObject(i);
            scaleList.add(new Scale(device));
        }
        return scaleList;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNetwork() {
        return network;
    }

    public String getBattery() {
        return battery;
    }

    public String getLastSyncTime() {
        return lastSyncTime;
    }

    public String getLastMeasurementTime() {
        return lastMeasurementTime;
    }

    public String getLastMeasurementTimeForMe() {
        return lastMeasurementTimeForMe;
    }

    public String getDefaultUnit() {
        return defaultUnit;
    }

    public int getBrightness() {
        return brightness;
    }

    public String getColor() {
        return color;
    }

    public String getVersion() {
        return version.name();
    }

    public ScaleVersion version() {
        return version;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }
}