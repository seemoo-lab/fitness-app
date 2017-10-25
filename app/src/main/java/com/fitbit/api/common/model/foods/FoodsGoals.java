package com.fitbit.api.common.model.foods;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: Alex Zh
 * Date: 02.09.11
 * Time: 16:53
 */
public class FoodsGoals {

    private int calories;

    public FoodsGoals() {
    }

    public FoodsGoals(int calories) {
        this.calories = calories;
    }

    public FoodsGoals(JSONObject json) throws JSONException {
        calories = json.getInt("calories");
    }

    public int getCalories() {
        return calories;
    }
}
