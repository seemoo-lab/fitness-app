package com.fitbit.api.common.model.heart;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by IntelliJ IDEA.
 * User: Kiryl
 * Date: 12/2/11
 * Time: 1:52 PM
 */
public class HeartAverage {

    private final String tracker;
    private final int heartRate;

    public HeartAverage(String tracker, int heartRate) {
        this.tracker = tracker;
        this.heartRate = heartRate;
    }

    public HeartAverage(JSONObject jsonObject) throws JSONException {
        this.tracker = jsonObject.getString("tracker");
        this.heartRate = jsonObject.getInt("heartRate");
    }

    public String getTracker() {
        return tracker;
    }

    public int getHeartRate() {
        return heartRate;
    }
}
