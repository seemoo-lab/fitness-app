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
public class FatLog {

    private long logId;
    private double fat;
    private LocalDate date;
    private String time;

    public FatLog(long logId, double fat, LocalDate date, String time) {
        this.logId = logId;
        this.fat = fat;
        this.date = date;
        this.time = time;
    }

    public FatLog(JSONObject jsonObject) throws JSONException {
        this.logId = jsonObject.getLong("logId");
        this.fat = jsonObject.getDouble("fat");
        this.date = FitbitApiService.getValidLocalDateOrNull(jsonObject.getString("date"));
        if(jsonObject.has("time")) {
            this.time = jsonObject.getString("time");
        }
    }

    public static List<FatLog> constructFatLogList(JSONArray array) throws JSONException {
        List<FatLog> fatLogList = new ArrayList<FatLog>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject fatLogReference = array.getJSONObject(i);
            fatLogList.add(new FatLog(fatLogReference));
        }
        return fatLogList;
    }

    public long getLogId() {
        return logId;
    }

    public double getFat() {
        return fat;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
