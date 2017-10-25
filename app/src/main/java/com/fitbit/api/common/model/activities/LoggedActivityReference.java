package com.fitbit.api.common.model.activities;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.http.Response;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoggedActivityReference extends ActivityReference {
    int calories;
    int duration;
    Double distance;
    Integer steps;

    public LoggedActivityReference(long activityId, String name, String description, Long activityParentId, String activityParentName,
                                   int calories, int duration, Double distance, Integer steps) {
        super(activityId, name, description, activityParentId, activityParentName);
        this.calories = calories;
        this.duration = duration;
        this.distance = distance;
        this.steps = steps;
    }

    public LoggedActivityReference(JSONObject json) throws JSONException {
        super(json);
        calories = json.getInt("calories");
        duration = json.getInt("duration");

        if (StringUtils.isNotBlank(json.optString("distance"))) {
            distance = json.getDouble("distance");
        }

        if (StringUtils.isNotBlank(json.optString("steps"))) {
            steps = json.getInt("steps");
        }
    }

    public static List<LoggedActivityReference> constructLoggedActivityReferenceList(Response res) throws FitbitAPIException {
        try {
            return jsonArrayToLoggedActivityReferenceList(res.asJSONArray());
         } catch (JSONException e) {
            throw new FitbitAPIException(e.getMessage() + ':' + res.asString(), e);
        }
    }

    private static List<LoggedActivityReference> jsonArrayToLoggedActivityReferenceList(JSONArray array) throws JSONException {
        List<LoggedActivityReference> loggedActivityReferenceList = new ArrayList<LoggedActivityReference>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonLoggedActivityReference = array.getJSONObject(i);
            loggedActivityReferenceList.add(new LoggedActivityReference(jsonLoggedActivityReference));
        }
        return loggedActivityReferenceList;
    }

    public int getCalories() {
        return calories;
    }

    public int getDuration() {
        return duration;
    }

    public Double getDistance() {
        return distance;
    }

    public Integer getSteps() {
        return steps;
    }
}