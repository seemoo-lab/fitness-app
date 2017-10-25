package com.fitbit.api.common.model.timeseries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class IntradayData {

    private String time;
    private double value;
    private Integer level;

    public IntradayData(String time, double value) {
        this.time = time;
        this.value = value;
    }

    public IntradayData(JSONObject json) throws JSONException {
        value = json.getDouble("value");
        time = json.getString("time");
        if(json.has("level")) {
            level = json.getInt("level");
        }
    }

    public String getTime() {
        return time;
    }

    public double getValue() {
        return value;
    }

    public Integer getLevel() {
        return level;
    }

    public static List<IntradayData> jsonArrayToDataList(JSONArray array) throws JSONException {
        List<IntradayData> intradayDataList = new ArrayList<IntradayData>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonData = array.getJSONObject(i);
            intradayDataList.add(new IntradayData(jsonData));
        }
        return intradayDataList;
    }
}
