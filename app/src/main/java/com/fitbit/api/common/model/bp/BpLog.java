package com.fitbit.api.common.model.bp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: Dzmitry Krutko
 * Date: Oct 15, 2011
 * Time: 6:55:07 PM
 */
public class BpLog {
    private final long logId;
    private final int systolic;
    private final int diastolic;
    private final String time;

    public BpLog(long logId, int systolic, int diastolic, String time) {
        this.logId = logId;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.time = time;
    }

    public BpLog(JSONObject jsonObject) throws JSONException {
        this.logId = jsonObject.getLong("logId");
        this.systolic = jsonObject.getInt("systolic");
        this.diastolic = jsonObject.getInt("diastolic");

        if (jsonObject.has("time")) {
            this.time = jsonObject.getString("time");
        } else {
            this.time = null;
        }
    }

    public long getLogId() {
        return logId;
    }

    public int getSystolic() {
        return systolic;
    }

    public int getDiastolic() {
        return diastolic;
    }

    public String getTime() {
        return time;
    }
}
