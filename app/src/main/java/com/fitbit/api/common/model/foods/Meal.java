package com.fitbit.api.common.model.foods;

import com.fitbit.api.FitbitAPIException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * User: gkutlu
 * Date: May 25, 2010
 * Time: 10:39:33 PM
 */
public class Meal {
    private final long id;
    private final String name;
    private final String description;
    private final List<LoggedFood> mealFoods;

    public Meal(long id, String name, String description, List<LoggedFood> mealFoods) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.mealFoods = mealFoods;
    }

    public Meal(JSONObject json) throws JSONException {
        id = json.getLong("id");
        name = json.getString("name");
        description = json.getString("description");
        mealFoods = LoggedFood.jsonArrayToLoggedFoodReferenceList(json.getJSONArray("mealFoods"));
    }

    public static List<Meal> constructMeals(JSONArray array) throws FitbitAPIException, JSONException {
        List<Meal> mealList = new ArrayList<Meal>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject meal = array.getJSONObject(i);
            mealList.add(new Meal(meal));
        }
        return mealList;
    }

    public final long getId() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final List<LoggedFood> getMealFoods() {
        return mealFoods;
    }
}
