package com.fitbit.api.common.model.devices;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.http.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Device {
    /**
     * Device id
     */
    private final String id;
    private final DeviceType type;
    /**
     * The battery level of the Fitbit device, one of Low, Medium, High, and Full. The level is "Low" when no
     * information is available.
     */
    private final String battery;

    private final String lastSyncTime;
    private final String deviceVersion;

    public Device(JSONObject json) throws JSONException {
        id = json.getString("id");
        type =  DeviceType.valueOf(json.getString("type"));
        battery = json.getString("battery");
        lastSyncTime = json.getString("lastSyncTime");
        deviceVersion = json.getString("deviceVersion");
    }

    public static List<Device> constructDeviceList(Response res) throws FitbitAPIException {
        try {
            return jsonArrayToDeviceList(res.asJSONArray());
        } catch (JSONException e) {
            throw new FitbitAPIException(e.getMessage() + ':' + res.asString(), e);
        }
    }

    private static List<Device> jsonArrayToDeviceList(JSONArray array) throws JSONException {
        List<Device> deviceList = new ArrayList<Device>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject device = array.getJSONObject(i);
            deviceList.add(new Device(device));
        }
        return deviceList;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type.name();
    }

    public DeviceType type() {
        return type;
    }

    public String getBattery() {
        return battery;
    }

    public String getLastSyncTime() {
        return lastSyncTime;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }
}
