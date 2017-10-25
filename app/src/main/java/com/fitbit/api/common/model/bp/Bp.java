package com.fitbit.api.common.model.bp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Bp {

    private final List<BpLog> bp;
    private final BpSummary average;

    public Bp(List<BpLog> bp, BpSummary average) {
        this.bp = bp;
        this.average = average;
    }

    public Bp(JSONObject jsonObject) throws JSONException {
        this.bp = jsonArrayToBpLogList(jsonObject.getJSONArray("bp"));
        if (jsonObject.has("average")) {
            this.average = new BpSummary(jsonObject.getJSONObject("average"));
        } else {
            this.average = null;
        }
    }

    private List<BpLog> jsonArrayToBpLogList(JSONArray array) throws JSONException {
        List<BpLog> bpLogList = new ArrayList<BpLog>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject bpLog = array.getJSONObject(i);
            bpLogList.add(new BpLog(bpLog));
        }
        return bpLogList;
    }

    public List<BpLog> getBp() {
        return bp;
    }

    public BpSummary getAverage() {
        return average;
    }
}