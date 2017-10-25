package com.fitbit.api.common.model.activities;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.http.Response;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Activities {

    private ActivitiesSummary summary;
    private List<ActivityLog> activities;
    private ActivityGoals activityGoals;

    public Activities(ActivitiesSummary summary, List<ActivityLog> activities, ActivityGoals activityGoals) {
        this.summary = summary;
        this.activities = activities;
        this.activityGoals = activityGoals;
    }

    public static Activities constructActivities(Response res) throws FitbitAPIException {
        try {
            ActivitiesSummary summary = new ActivitiesSummary(res.asJSONObject().getJSONObject("summary"));
            List<ActivityLog> activities = jsonArrayToActivityList(res.asJSONObject().getJSONArray("activities"));
            JSONObject goalsJson = res.asJSONObject().optJSONObject("goals");
            ActivityGoals activityGoals = goalsJson != null ? new ActivityGoals(goalsJson) : null;
            return new Activities(summary, activities, activityGoals);
         } catch (JSONException e) {
            throw new FitbitAPIException(e.getMessage() + ':' + res.asString(), e);
        }        
    }

    private static List<ActivityLog> jsonArrayToActivityList(JSONArray array) throws JSONException {
        List<ActivityLog> activityList = new ArrayList<ActivityLog>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject activity = array.getJSONObject(i);
            activityList.add(new ActivityLog(activity));
        }
        return activityList;
    }

    public ActivitiesSummary getSummary() {
        return summary;
    }

    public List<ActivityLog> getActivities() {
        return activities;
    }

    public ActivityGoals getActivityGoals() {
        return activityGoals;
    }
}
