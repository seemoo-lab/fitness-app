package com.fitbit.api.common.model.body;

import org.json.JSONException;
import org.json.JSONObject;

public class BodyGoals {

    private Double weight;
    private Double fat;

    public BodyGoals(JSONObject json) throws JSONException {
        if (json.has("weight")) {
            weight = json.getDouble("weight");
        }
        if (json.has("fat")) {
            fat = json.getDouble("fat");
        }
    }

    public Double getWeight() {
        return weight;
    }

    public Double getFat() {
        return fat;
    }
}
