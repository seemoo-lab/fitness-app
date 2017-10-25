package com.fitbit.api.common.model.heart;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by IntelliJ IDEA.
 * User: Kiryl
 * Date: 12/2/11
 * Time: 1:52 PM
 */
public class HeartLog {

    private final long logId;
    private final String tracker;
    private final int heartRate;
    private final String time;

    public HeartLog(long logId, String tracker, int heartRate, String time) {
        this.logId = logId;
        this.tracker = tracker;
        this.heartRate = heartRate;
        this.time = time;
    }

    public HeartLog(JSONObject jsonObject) throws JSONException {
        this.logId = jsonObject.getLong("logId");
        this.tracker = jsonObject.getString("tracker");
        this.heartRate = jsonObject.getInt("heartRate");

        if (jsonObject.has("time")) {
            this.time = jsonObject.getString("time");
        } else {
            this.time = null;
        }
    }

    public long getLogId() {
        return logId;
    }

    public String getTracker() {
        return tracker;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public String getTime() {
        return time;
    }
}
