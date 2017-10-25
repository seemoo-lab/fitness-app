package com.fitbit.api.common.model.activities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ActivitiesSummary {
    private int caloriesOut;
    private int activityCalories;
    private int marginalCalories;
    private int steps;
    private Integer floors = null;
    private Double elevation = null;
    private int sedentaryMinutes;
    private int lightlyActiveMinutes;
    private int fairlyActiveMinutes;
    private int veryActiveMinutes;
    private List<ActivityDistance> distances;

    public ActivitiesSummary(JSONObject json) throws JSONException {
        caloriesOut = json.getInt("caloriesOut");
        activityCalories = json.getInt("activityCalories");
        marginalCalories = json.getInt("marginalCalories");
        steps = json.getInt("steps");
        // for now its optional
        if (json.has("floors")) {
            floors = json.getInt("floors");
        }

        // for now its optional
        if (json.has("elevation")) {
            elevation = json.getDouble("elevation");
        }
        sedentaryMinutes = json.getInt("sedentaryMinutes");
        lightlyActiveMinutes = json.getInt("lightlyActiveMinutes");
        fairlyActiveMinutes = json.getInt("fairlyActiveMinutes");
        veryActiveMinutes = json.getInt("veryActiveMinutes");
        distances = jsonArrayToActivityList(json.getJSONArray("distances"));
    }

    private static List<ActivityDistance> jsonArrayToActivityList(JSONArray array) throws JSONException {
        List<ActivityDistance> trends = new ArrayList<ActivityDistance>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject distance = array.getJSONObject(i);
            trends.add(new ActivityDistance(distance));
        }
        return trends;
    }

    public int getCaloriesOut() {
        return caloriesOut;
    }

    public void setCaloriesOut(int caloriesOut) {
        this.caloriesOut = caloriesOut;
    }

    public int getActivityCalories() {
        return activityCalories;
    }

    public void setActivityCalories(int activityCalories) {
        this.activityCalories = activityCalories;
    }

    public int getMarginalCalories() {
        return marginalCalories;
    }

    public void setMarginalCalories(int marginalCalories) {
        this.marginalCalories = marginalCalories;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public Integer getFloors() {
        return floors;
    }

    public Double getElevation() {
        return elevation;
    }

    public int getSedentaryMinutes() {
        return sedentaryMinutes;
    }

    public void setSedentaryMinutes(int sedentaryMinutes) {
        this.sedentaryMinutes = sedentaryMinutes;
    }

    public int getLightlyActiveMinutes() {
        return lightlyActiveMinutes;
    }

    public void setLightlyActiveMinutes(int lightlyActiveMinutes) {
        this.lightlyActiveMinutes = lightlyActiveMinutes;
    }

    public int getFairlyActiveMinutes() {
        return fairlyActiveMinutes;
    }

    public void setFairlyActiveMinutes(int fairlyActiveMinutes) {
        this.fairlyActiveMinutes = fairlyActiveMinutes;
    }

    public int getVeryActiveMinutes() {
        return veryActiveMinutes;
    }

    public void setVeryActiveMinutes(int veryActiveMinutes) {
        this.veryActiveMinutes = veryActiveMinutes;
    }

    public List<ActivityDistance> getDistances() {
        return distances;
    }

    public void setDistances(List<ActivityDistance> distances) {
        this.distances = distances;
    }
}
