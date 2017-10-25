package com.fitbit.api.common.model.activities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Activity extends DisplayableActivity {

    private  String accessLevel;
    private boolean hasSpeed;
    private Double mets;

    private List<ActivityLevel> activityLevels;

    public Activity(JSONObject json) throws JSONException {
        super(json);
        accessLevel = json.getString("accessLevel");
        hasSpeed = json.getBoolean("hasSpeed");
        if(json.has("mets")) {
            mets = json.getDouble("mets");
        }
        if (json.has("activityLevels")) {
            activityLevels = jsonArrayToActivityLevelList(json.getJSONArray("activityLevels"));
        }
    }

    private static List<ActivityLevel> jsonArrayToActivityLevelList(JSONArray array) throws JSONException {
        List<ActivityLevel> activityLevelList = new ArrayList<ActivityLevel>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonActivityLevel = array.getJSONObject(i);
            activityLevelList.add(new ActivityLevel(jsonActivityLevel));
        }
        return activityLevelList;
    }

    public static Activity constructActivity(JSONObject json) throws JSONException {
        return new Activity(json.getJSONObject("activity"));
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public boolean getHasSpeed() {
        return hasSpeed;
    }

    public Double getMets() {
        return mets;
    }

    public List<ActivityLevel> getActivityLevels() {
        return activityLevels;
    }

    public boolean hasLevels() {
        return !activityLevels.isEmpty();
    }    
}
