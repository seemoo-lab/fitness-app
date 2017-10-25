package com.fitbit.api.common.model.foods;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by IntelliJ IDEA.
 * User: Kiryl
 * Date: 6/13/11
 * Time: 11:43 AM
 */
public class WaterLog {

    private final long logId;
    private final double amount;

    public WaterLog(long logId, double amount) {
        this.logId = logId;
        this.amount = amount;
    }

    public WaterLog(JSONObject jsonObject) throws JSONException {
        this.logId = jsonObject.getLong("logId");
        this.amount = jsonObject.getDouble("amount");
    }

    public long getLogId() {
        return logId;
    }

    public double getAmount() {
        return amount;
    }
}
