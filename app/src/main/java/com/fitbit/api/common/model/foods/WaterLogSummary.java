package com.fitbit.api.common.model.foods;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by IntelliJ IDEA.
 * User: Kiryl
 * Date: 6/13/11
 * Time: 11:43 AM
 */
public class WaterLogSummary {

    private final double water;

    public WaterLogSummary(double water) {
        this.water = water;
    }

    public WaterLogSummary(JSONObject jsonObject) throws JSONException {
        this.water = jsonObject.getDouble("water");
    }

    public double getWater() {
        return water;
    }
}
