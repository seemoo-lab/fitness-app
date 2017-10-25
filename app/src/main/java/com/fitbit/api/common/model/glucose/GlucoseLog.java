package com.fitbit.api.common.model.glucose;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by IntelliJ IDEA.
 * User: Kiryl
 * Date: 11/18/11
 * Time: 10:43 AM
 */
public class GlucoseLog {

    private final String tracker;
    private final double glucose;
    private final String time;

    public GlucoseLog(String tracker, double glucose, String time) {
        this.tracker = tracker;
        this.glucose = glucose;
        this.time = time;
    }

    public GlucoseLog(JSONObject jsonObject) throws JSONException {
        tracker = jsonObject.getString("tracker");
        glucose = jsonObject.getDouble("glucose");
        if(jsonObject.has("time")) {
            time = jsonObject.getString("time");
        } else {
            time = null;
        }
    }

    public String getTracker() {
        return tracker;
    }

    public double getGlucose() {
        return glucose;
    }

    public String getTime() {
        return time;
    }
}
