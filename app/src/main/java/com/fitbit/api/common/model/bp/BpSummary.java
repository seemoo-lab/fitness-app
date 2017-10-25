package com.fitbit.api.common.model.bp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: Dzmitry Krutko
 * Date: Oct 15, 2011
 * Time: 8:46:21 PM
 */
public class BpSummary {
    private final int systolic;
    private final int diastolic;
    private final String condition;

    public BpSummary(int systolic, int diastolic, String condition) {
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.condition = condition;
    }

    public BpSummary(JSONObject jsonObject) throws JSONException {
        this.systolic = jsonObject.getInt("systolic");
        this.diastolic = jsonObject.getInt("diastolic");
        this.condition = jsonObject.getString("condition");
    }

    public int getSystolic() {
        return systolic;
    }

    public int getDiastolic() {
        return diastolic;
    }

    public String getCondition() {
        return condition;
    }
}
