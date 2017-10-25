package com.fitbit.api.common.model.foods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kiryl
 * Date: 6/13/11
 * Time: 11:43 AM
 */
public class Water {

    private final List<WaterLog> water;
    private final WaterLogSummary summary;

    public Water(List<WaterLog> water, WaterLogSummary summary) {
        this.water = water;
        this.summary = summary;
    }

    public Water(JSONObject jsonObject) throws JSONException {
        this.water = jsonArrayToWaterLogList(jsonObject.getJSONArray("water"));
        this.summary = new WaterLogSummary(jsonObject.getJSONObject("summary"));

    }

    private List<WaterLog> jsonArrayToWaterLogList(JSONArray array) throws JSONException {
        List<WaterLog> waterLogList = new ArrayList<WaterLog>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject waterLog = array.getJSONObject(i);
            waterLogList.add(new WaterLog(waterLog));
        }
        return waterLogList;
    }

    public List<WaterLog> getWater() {
        return water;
    }

    public WaterLogSummary getSummary() {
        return summary;
    }
}
