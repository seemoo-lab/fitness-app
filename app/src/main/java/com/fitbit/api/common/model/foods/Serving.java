package com.fitbit.api.common.model.foods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kiryl
 * Date: 1/19/12
 * Time: 5:24 AM
 */
public class Serving {
    private int unitId;
    private double servingSize;
    private double multiplier;
    private FoodUnit unit;

    public Serving(int unitId, double servingSize, double multiplier, FoodUnit unit) {
        this.unitId = unitId;
        this.servingSize = servingSize;
        this.multiplier = multiplier;
        this.unit = unit;
    }

    public Serving(JSONObject jsonObject) throws JSONException {
        this.unitId = jsonObject.getInt("unitId");
        this.servingSize = jsonObject.getDouble("servingSize");
        this.multiplier = jsonObject.getDouble("multiplier");
        this.unit = new FoodUnit(jsonObject.getJSONObject("unit"));
    }

    public static List<Serving> jsonArrayToServingList(JSONArray array) throws JSONException {
        List<Serving> servingList = new ArrayList<Serving>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject serving = array.getJSONObject(i);
            servingList.add(new Serving(serving));
        }
        return servingList;
    }

    public int getUnitId() {
        return unitId;
    }

    public double getServingSize() {
        return servingSize;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public FoodUnit getUnit() {
        return unit;
    }
}