package com.fitbit.api.common.model.sleep;

import org.json.JSONException;
import org.json.JSONObject;

public class SleepSummary {

    int totalSleepRecords = 0;
    int totalMinutesAsleep = 0;
    int totalTimeInBed = 0;

    public SleepSummary() {
        this.totalSleepRecords = 0;
        this.totalMinutesAsleep = 0;
        this.totalTimeInBed = 0;
    }

    public SleepSummary(JSONObject json) throws JSONException {
        totalSleepRecords = json.getInt("totalSleepRecords");
        totalMinutesAsleep = json.getInt("totalMinutesAsleep");
        totalTimeInBed = json.getInt("totalTimeInBed");
    }

    public void addSleepLog(SleepLog sleepLog) {
        this.totalSleepRecords ++;
        this.totalMinutesAsleep = this.totalMinutesAsleep + sleepLog.getMinutesAsleep();
        this.totalTimeInBed = this.totalTimeInBed + sleepLog.getTimeInBed();
    }

    public int getTotalSleepRecords() {
        return totalSleepRecords;
    }

    public int getTotalMinutesAsleep() {
        return totalMinutesAsleep;
    }

    public int getTotalTimeInBed() {
        return totalTimeInBed;
    }
}
