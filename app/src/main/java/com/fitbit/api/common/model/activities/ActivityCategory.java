package com.fitbit.api.common.model.activities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityCategory {

    private int id;

    private String name;

    private List<ActivityCategory> subCategories;

    private List<DisplayableActivity> activities;

    public ActivityCategory(JSONObject json) throws JSONException {
        this.id = json.getInt("id");
        this.name = json.getString("name");
        if (json.has("subCategories")) {
            this.subCategories = new ArrayList<ActivityCategory>();
            JSONArray jsonArraySubCategories = json.getJSONArray("subCategories");
            JSONObject jsonSubCategory;
            for (int i = 0; i < jsonArraySubCategories.length(); i++) {
                jsonSubCategory = jsonArraySubCategories.getJSONObject(i);
                this.subCategories.add(new ActivityCategory(jsonSubCategory));

            }
        }
        if (json.has("activities")) {
            this.activities = new ArrayList<DisplayableActivity>();
            JSONArray jsonArrayActivities = json.getJSONArray("activities");
            JSONObject jsonActivity;
            for (int i = 0; i < jsonArrayActivities.length(); i++) {
                jsonActivity = jsonArrayActivities.getJSONObject(i);
                this.activities.add(new Activity(jsonActivity));

            }
        }
    }

    public static List<ActivityCategory> jsonArrayToActivityCategoryList(JSONArray array) throws JSONException {
        List<ActivityCategory> activityCategories = new ArrayList<ActivityCategory>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonActivityCategory = array.getJSONObject(i);
            activityCategories.add(new ActivityCategory(jsonActivityCategory));
        }
        return activityCategories;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public List<ActivityCategory> getSubCategories() {
        return subCategories;
    }

    public List<DisplayableActivity> getActivities() {
        return activities;
    }
}
