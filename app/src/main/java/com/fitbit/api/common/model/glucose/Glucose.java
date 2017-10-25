package com.fitbit.api.common.model.glucose;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kiryl
 * Date: 11/18/11
 * Time: 10:43 AM
 */
public class Glucose {

    private final List<GlucoseLog> glucoseLog;
    private final Double hba1c;

    public Glucose(List<GlucoseLog> glucoseLog, Double hba1c) {
        this.glucoseLog = glucoseLog;
        this.hba1c = hba1c;
    }

    public Glucose(JSONObject jsonObject) throws JSONException {
        glucoseLog = jsonObject.has("glucose") ? jsonArrayToGlucoseLogList(jsonObject.getJSONArray("glucose")) : new ArrayList<GlucoseLog>();
        if (jsonObject.has("hba1c")) {
            hba1c = jsonObject.getDouble("hba1c");
        } else {
            hba1c = null;
        }
    }

    private List<GlucoseLog> jsonArrayToGlucoseLogList(JSONArray array) throws JSONException {
        List<GlucoseLog> glucoseLogList = new ArrayList<GlucoseLog>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject glucoseLog = array.getJSONObject(i);
            glucoseLogList.add(new GlucoseLog(glucoseLog));
        }
        return glucoseLogList;
    }

    public List<GlucoseLog> getGlucoseLog() {
        return glucoseLog;
    }

    public Double getHba1c() {
        return hba1c;
    }
}
