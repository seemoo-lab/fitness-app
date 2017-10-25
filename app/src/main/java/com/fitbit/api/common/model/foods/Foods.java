package com.fitbit.api.common.model.foods;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.http.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * User: gkutlu
 * Date: Mar 2, 2010
 * Time: 7:24:32 PM
 */
public class Foods {
    private FoodsSummary summary;
    private List<FoodLog> foods;
    private FoodsGoals foodsGoals;

    public Foods() {
    }

    public Foods(FoodsSummary summary, List<FoodLog> foods) {
        this.summary = summary;
        this.foods = foods;
    }

    public Foods(FoodsSummary summary, List<FoodLog> foods, FoodsGoals foodsGoals) {
        this.summary = summary;
        this.foods = foods;
        this.foodsGoals = foodsGoals;
    }

    public static Foods constructFoods(Response res) throws FitbitAPIException {
        try {
            JSONObject json = res.asJSONObject();
            FoodsSummary summary = new FoodsSummary(json.getJSONObject("summary"));
            JSONObject goalsJSON = json.optJSONObject("goals");
            FoodsGoals foodsGoals = goalsJSON != null ? new FoodsGoals(goalsJSON) : null;
            List<FoodLog> foods = jsonArrayToFoodLogList(json.getJSONArray("foods"));
            return new Foods(summary, foods, foodsGoals);
         } catch (JSONException e) {
            throw new FitbitAPIException(e.getMessage() + ':' + res.asString(), e);
        }
    }

    public static List<FoodLog> constructFoodlogList(Response res) throws FitbitAPIException {
        return constructFoodLogList(res, "foods");
    }

    public static List<FoodLog> constructFoodLogList(Response res, String arrayName) throws FitbitAPIException {
        try {
            JSONObject json = res.asJSONObject();
            return jsonArrayToFoodLogList(json.getJSONArray(arrayName));
         } catch (JSONException e) {
            throw new FitbitAPIException(e.getMessage() + ':' + res.asString(), e);
        }
    }

    private static List<FoodLog> jsonArrayToFoodLogList(JSONArray array) throws JSONException {
        List<FoodLog> foodList = new ArrayList<FoodLog>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject food = array.getJSONObject(i);
            foodList.add(new FoodLog(food));
        }
        return foodList;
    }

    public FoodsSummary getSummary() {
        return summary;
    }

    public List<FoodLog> getFoods() {
        return foods;
    }

    public FoodsGoals getFoodsGoals() {
        return foodsGoals;
    }
}