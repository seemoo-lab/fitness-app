package com.fitbit.api.common.model.sleep;

import org.json.JSONException;
import org.json.JSONObject;

public class SleepLog {
    long logId;
    String startTime;
    boolean isMainSleep;
    long duration;
    int minutesToFallAsleep;
    int minutesAsleep;
    int minutesAwake;
    int minutesAfterWakeup;
    int awakeningsCount;
    int timeInBed;
    int efficiency;

    public SleepLog(long logId, String startTime, boolean mainSleep, long duration, int minutesToFallAsleep,
                    int minutesAsleep, int minutesAwake, int minutesAfterWakeup, int awakeningsCount, int timeInBed,
                    int efficiency) {
        this.logId = logId;
        this.startTime = startTime;
        isMainSleep = mainSleep;
        this.duration = duration;
        this.minutesToFallAsleep = minutesToFallAsleep;
        this.minutesAsleep = minutesAsleep;
        this.minutesAwake = minutesAwake;
        this.minutesAfterWakeup = minutesAfterWakeup;
        this.awakeningsCount = awakeningsCount;
        this.timeInBed = timeInBed;
        this.efficiency = efficiency;
    }

    public SleepLog(JSONObject json) throws JSONException {
        logId = json.getLong("logId");
        startTime = json.getString("startTime");
        isMainSleep = json.getBoolean("isMainSleep");
        duration = json.getLong("duration");
        minutesToFallAsleep = json.getInt("minutesToFallAsleep");
        minutesAsleep = json.getInt("minutesAsleep");
        minutesAwake = json.getInt("minutesAwake");
        minutesAfterWakeup = json.getInt("minutesAfterWakeup");
        awakeningsCount = json.getInt("awakeningsCount");
        timeInBed = json.getInt("timeInBed");
        efficiency = json.getInt("efficiency");
    }

    public long getLogId() {
        return logId;
    }

    public String getStartTime() {
        return startTime;
    }

    public boolean isMainSleep() {
        return isMainSleep;
    }

    public long getDuration() {
        return duration;
    }

    public int getMinutesToFallAsleep() {
        return minutesToFallAsleep;
    }

    public int getMinutesAsleep() {
        return minutesAsleep;
    }

    public int getMinutesAwake() {
        return minutesAwake;
    }

    public int getMinutesAfterWakeup() {
        return minutesAfterWakeup;
    }

    public int getAwakeningsCount() {
        return awakeningsCount;
    }

    public int getTimeInBed() {
        return timeInBed;
    }

    public int getEfficiency() {
        return efficiency;
    }
}
