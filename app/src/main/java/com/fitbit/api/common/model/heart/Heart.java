package com.fitbit.api.common.model.heart;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Heart {

    private final List<HeartLog> heartLog;
    private final List<HeartAverage> trackerAverage;

    public Heart(List<HeartLog> heartLog, List<HeartAverage> trackerAverage) {
        this.heartLog = heartLog;
        this.trackerAverage = trackerAverage;
    }

    public Heart(JSONObject jsonObject) throws JSONException {
        this.heartLog = jsonArrayToHeartLogList(jsonObject.getJSONArray("heart"));
        this.trackerAverage = jsonArrayToAverageList(jsonObject.getJSONArray("average"));
    }

    private List<HeartLog> jsonArrayToHeartLogList(JSONArray array) throws JSONException {
        List<HeartLog> heartLogList = new ArrayList<HeartLog>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject heartLog = array.getJSONObject(i);
            heartLogList.add(new HeartLog(heartLog));
        }
        return heartLogList;
    }

    private List<HeartAverage> jsonArrayToAverageList(JSONArray array) throws JSONException {
        List<HeartAverage> heartAverageList = new ArrayList<HeartAverage>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject heartAverage = array.getJSONObject(i);
            heartAverageList.add(new HeartAverage(heartAverage));
        }
        return heartAverageList;
    }

    public List<HeartLog> getHeartLog() {
        return heartLog;
    }

    public List<HeartAverage> getTrackerAverage() {
        return trackerAverage;
    }
}