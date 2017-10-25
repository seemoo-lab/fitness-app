package com.fitbit.api.common.model.devices;

import com.fitbit.api.common.service.FitbitApiService;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScaleMeasurementLog {

    private Long logId;
    private Double fat;
    private Double weight;
    private LocalDate date;
    private LocalTime time;
    private String userId;
    private String scaleUserName;

    public ScaleMeasurementLog(Long logId, Double fat, Double weight, LocalDate date, LocalTime time, String userId, String scaleUserName) {
        this.logId = logId;
        this.fat = fat;
        this.weight = weight;
        this.date = date;
        this.time = time;
        this.userId = userId;
        this.scaleUserName = scaleUserName;
    }

    public ScaleMeasurementLog(JSONObject jsonObject) throws JSONException {
        this.logId = jsonObject.getLong("logId");
        if (jsonObject.has("fat")) {
            this.fat = jsonObject.getDouble("fat");
        }
        if (jsonObject.has("weight")) {
            this.weight = jsonObject.getDouble("weight");
        }
        this.date = FitbitApiService.LOCAL_DATE_FORMATTER.parseLocalDate(jsonObject.getString("date"));
        this.time = FitbitApiService.LOCAL_TIME_HOURS_MINUTES_SECONDS_FORMATTER.parseLocalTime(jsonObject.getString("time"));
        if (jsonObject.has("userId")) {
            this.userId = jsonObject.getString("userId");
        }
        this.scaleUserName = jsonObject.getString("scaleUserName");
    }

    public static List<ScaleMeasurementLog> jsonArrayToMeasurementLogList(JSONArray array) throws JSONException {
        List<ScaleMeasurementLog> scaleMeasurementLogList = new ArrayList<ScaleMeasurementLog>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject scaleMeasurementReference = array.getJSONObject(i);
            scaleMeasurementLogList.add(new ScaleMeasurementLog(scaleMeasurementReference));
        }
        return scaleMeasurementLogList;
    }

    public Long getLogId() {
        return logId;
    }

    public Double getFat() {
        return fat;
    }

    public Double getWeight() {
        return weight;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getUserId() {
        return userId;
    }

    public String getScaleUserName() {
        return scaleUserName;
    }
}
