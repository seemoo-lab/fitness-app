package com.fitbit.api.common.model.timeseries;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class IntradayDataset {

    private int datasetInterval;
    private List<IntradayData> dataset;

    public IntradayDataset(int datasetInterval, List<IntradayData> dataset) {
        this.datasetInterval = datasetInterval;
        this.dataset = dataset;
    }

    public IntradayDataset(JSONObject json) throws JSONException {
        this.datasetInterval = json.getInt("datasetInterval");
        this.dataset = IntradayData.jsonArrayToDataList(json.getJSONArray("dataset"));
    }

    public int getDatasetInterval() {
        return datasetInterval;
    }

    public List<IntradayData> getDataset() {
        return dataset;
    }

}
