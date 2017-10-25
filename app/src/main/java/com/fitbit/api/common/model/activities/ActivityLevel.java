package com.fitbit.api.common.model.activities;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityLevel extends DisplayableActivity{

    private float minSpeedMPH;
    private float maxSpeedMPH;
    private double mets;

    @SuppressWarnings({"NumericCastThatLosesPrecision"})
    public ActivityLevel(JSONObject json) throws JSONException {
        super(json);
        minSpeedMPH = (float)json.getDouble("minSpeedMPH");
        maxSpeedMPH = (float)json.getDouble("maxSpeedMPH");
        mets = json.getDouble("mets");
    }

    public float getMaxSpeedMPH() {
        return maxSpeedMPH;
    }

    public float getMinSpeedMPH() {
        return minSpeedMPH;
    }

    public double getMets() {
        return mets;
    }
}
