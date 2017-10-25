package com.fitbit.api.common.model.foods;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * User: gkutlu
 * Date: Mar 2, 2010
 * Time: 7:26:43 PM
 */
public class FoodsSummary extends NutritionalValues {
    double water;

    public FoodsSummary(int calories, double fat, double fiber, double carbs, double sodium, double protein, double water) {
        super(calories, fat, fiber, carbs, sodium, protein);
        this.water = water;
    }

    public FoodsSummary(JSONObject json) throws JSONException {
        super(json);
        water = json.getInt("water");
    }

    public double getWater() {
        return water;
    }
}