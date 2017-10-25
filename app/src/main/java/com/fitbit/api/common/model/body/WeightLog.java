package com.fitbit.api.common.model.body;

import com.fitbit.api.common.service.FitbitApiService;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Kiryl
 * Date: 2/8/12
 * Time: 3:07 AM
 */
public class WeightLog {

    private long logId;
    private double weight;
    private double bmi;
    private LocalDate date;
    private String time;

    public WeightLog(long logId, double weight, double bmi, LocalDate date, String time) {
        this.logId = logId;
        this.weight = weight;
        this.bmi = bmi;
        this.date = date;
        this.time = time;
    }

    public WeightLog(JSONObject jsonObject) throws JSONException {
        this.logId = jsonObject.getLong("logId");
        this.weight = jsonObject.getDouble("weight");
        this.bmi = jsonObject.getDouble("bmi");
        this.date = FitbitApiService.getValidLocalDateOrNull(jsonObject.getString("date"));
        if(jsonObject.has("time")) {
            this.time = jsonObject.getString("time");
        }
    }

    public static List<WeightLog> constructWeightLogList(JSONArray array) throws JSONException {
        List<WeightLog> weightLogList = new ArrayList<WeightLog>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject weightLogReference = array.getJSONObject(i);
            weightLogList.add(new WeightLog(weightLogReference));
        }
        return weightLogList;
    }

    public long getLogId() {
        return logId;
    }

    public double getWeight() {
        return weight;
    }

    public double getBmi() {
        return bmi;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
