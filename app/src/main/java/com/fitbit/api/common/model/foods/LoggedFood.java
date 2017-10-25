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
 * Date: May 22, 2010
 * Time: 4:27:21 PM
 */
public class LoggedFood {
    private final long foodId;
    private final String name;
    private final String brand;
    private final String accessLevel;
    private final int[] units;

    private final int calories;
    private final double amount;
    private final FoodUnit unit;
    private final byte mealTypeId;

    public LoggedFood(long foodId, String name, String brand, String accessLevel, int calories, double amount, FoodUnit unit, byte mealTypeId, int[] units) {
        this.foodId = foodId;
        this.name = name;
        this.brand = brand;
        this.accessLevel = accessLevel;
        this.units = units;
        this.calories = calories;
        this.amount = amount;
        this.unit = unit;
        this.mealTypeId = mealTypeId;
    }

    public LoggedFood(JSONObject json) throws JSONException {
        foodId = json.getLong("foodId");
        name = json.getString("name");
        brand = json.getString("brand");
        units = jsonArrayToUnitIdArray(json.getJSONArray("units"));
        accessLevel = json.optString("accessLevel");
        calories = json.getInt("calories");
        amount = json.getDouble("amount");
        unit = new FoodUnit(json.getJSONObject("unit"));
        //noinspection NumericCastThatLosesPrecision
        mealTypeId = (byte) json.getInt("mealTypeId");
    }

    public static List<LoggedFood> constructLoggedFoodReferenceList(Response res) throws FitbitAPIException {
        try {
            return jsonArrayToLoggedFoodReferenceList(res.asJSONArray());
        } catch (JSONException e) {
            throw new FitbitAPIException(e.getMessage() + ':' + res.asString(), e);
        }
    }

    public static List<LoggedFood> jsonArrayToLoggedFoodReferenceList(JSONArray array) throws JSONException {
        List<LoggedFood> loggedFoodList = new ArrayList<LoggedFood>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonLoggedFoodReference = array.getJSONObject(i);
            loggedFoodList.add(new LoggedFood(jsonLoggedFoodReference));
        }
        return loggedFoodList;
    }

    private static int[] jsonArrayToUnitIdArray(JSONArray array) throws JSONException {
        int[] units = new int[array.length()];
        for (int i = 0; i < array.length(); i++) {
            units[i] = array.getInt(i);
        }
        return units;
    }

    public final int getCalories() {
        return calories;
    }

    public final double getAmount() {
        return amount;
    }

    public final FoodUnit getUnit() {
        return unit;
    }

    public final byte getMealTypeId() {
        return mealTypeId;
    }

    public long getFoodId() {
        return foodId;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getAccessLevel() {
        return accessLevel;
    }

    public int[] getUnits() {
        return units;
    }
}