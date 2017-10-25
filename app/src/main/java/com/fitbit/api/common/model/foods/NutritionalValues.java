package com.fitbit.api.common.model.foods;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: gkutlu
 * Date: Sep 17, 2010
 * Time: 1:41:26 PM
 */
public class NutritionalValues {
    int calories;
    double fat;
    double fiber;
    double carbs;
    double sodium;
    double protein;

    public NutritionalValues(int calories, double fat, double fiber, double carbs, double sodium, double protein) {
        this.calories = calories;
        this.fat = fat;
        this.fiber = fiber;
        this.carbs = carbs;
        this.sodium = sodium;
        this.protein = protein;
    }

    public NutritionalValues(JSONObject json) throws JSONException {
        calories = json.getInt("calories");
        fat = json.getDouble("fat");
        fiber = json.getDouble("fiber");
        carbs = json.getDouble("carbs");
        sodium = json.getDouble("sodium");
        protein = json.getDouble("protein");
    }

    public int getCalories() {
        return calories;
    }

    public double getFat() {
        return fat;
    }

    public double getFiber() {
        return fiber;
    }

    public double getCarbs() {
        return carbs;
    }

    public double getSodium() {
        return sodium;
    }

    public double getProtein() {
        return protein;
    }

}
