package com.fitbit.api.common.model.activities;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class ActivityGoals {

    private Integer caloriesOut;
    private Integer steps;
    private Double distance;
    private Integer activeMinutes;
    private Integer floors;

    public ActivityGoals() {
    }

    public ActivityGoals(JSONObject json) throws JSONException {
        if (StringUtils.isNotBlank(json.optString("caloriesOut"))) {
            caloriesOut = json.getInt("caloriesOut");
        }
        if (StringUtils.isNotBlank(json.optString("steps"))) {
            steps = json.getInt("steps");
        }
        if (StringUtils.isNotBlank(json.optString("distance"))) {
            distance = json.getDouble("distance");
        }
        if (StringUtils.isNotBlank(json.optString("activeMinutes"))) {
            activeMinutes = json.getInt("activeMinutes");
        }
        if (StringUtils.isNotBlank(json.optString("floors"))) {
            floors = json.getInt("floors");
        }
    }

    public Integer getCaloriesOut() {
        return caloriesOut;
    }

    public Integer getSteps() {
        return steps;
    }

    public Double getDistance() {
        return distance;
    }

    public Integer getActiveMinutes() {
        return activeMinutes;
    }

    public Integer getFloors() {
        return floors;
    }

    public boolean hasAnyValue() {
        return caloriesOut != null || steps != null || distance != null || activeMinutes != null || floors != null;
    }
}
