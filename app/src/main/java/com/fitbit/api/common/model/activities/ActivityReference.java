package com.fitbit.api.common.model.activities;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.http.Response;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityReference {
    long activityId;
    String name;
    String description;
    Long activityParentId;
    String activityParentName;

    public ActivityReference(long activityId, String name, String description, Long activityParentId, String activityParentName) {
        this.activityId = activityId;
        this.name = name;
        this.description = description;
        this.activityParentId = activityParentId;
        this.activityParentName = activityParentName;
    }

    public ActivityReference(JSONObject json) throws JSONException {
        activityId = json.getLong("activityId");
        name = json.getString("name");
        description = json.getString("description");

        if (StringUtils.isNotBlank(json.optString("activityParentId"))) {
            activityParentId = json.getLong("activityParentId");
        }

        activityParentName = json.optString("activityParentName");
    }

    public static List<ActivityReference> constructActivityReferenceList(Response res) throws FitbitAPIException {
        try {
            return jsonArrayToActivityReferenceList(res.asJSONArray());
         } catch (JSONException e) {
            throw new FitbitAPIException(e.getMessage() + ':' + res.asString(), e);
        }
    }

    private static List<ActivityReference> jsonArrayToActivityReferenceList(JSONArray array) throws JSONException {
        List<ActivityReference> activityReferenceList = new ArrayList<ActivityReference>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonActivityReference = array.getJSONObject(i);
            activityReferenceList.add(new ActivityReference(jsonActivityReference));
        }
        return activityReferenceList;
    }

    public long getActivityId() {
        return activityId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getActivityParentId() {
        return activityParentId;
    }

    public String getActivityParentName() {
        return activityParentName;
    }
}