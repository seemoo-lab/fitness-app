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
 * Date: Sep 11, 2010
 * Time: 11:55:00 AM
 */
public class FoodUnit {
    private final int id;
    private final String name;
    private final String plural;

    public FoodUnit(int id, String name, String plural) {
        this.id = id;
        this.name = name;
        this.plural = plural;
    }

    public FoodUnit(JSONObject json) throws JSONException {
        id = json.getInt("id");
        name = json.getString("name");
        plural = json.getString("plural");
    }

    public static List<FoodUnit> constructFoodUnitList(Response res) throws FitbitAPIException {
        try {
            return jsonArrayToFoodUnitList(res.asJSONArray());
        } catch (JSONException e) {
           throw new FitbitAPIException(e.getMessage() + ':' + res.asString(), e);
       }
    }

    public static List<FoodUnit> jsonArrayToFoodUnitList(JSONArray array) throws JSONException {
        List<FoodUnit> unitList = new ArrayList<FoodUnit>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject foodUnit = array.getJSONObject(i);
            unitList.add(new FoodUnit(foodUnit));
        }
        return unitList;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPlural() {
        return plural;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodUnit foodUnit = (FoodUnit) o;
        return id == foodUnit.id && name.equals(foodUnit.name) && plural.equals(foodUnit.plural);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (plural != null ? plural.hashCode() : 0);
        return result;
    }
}


