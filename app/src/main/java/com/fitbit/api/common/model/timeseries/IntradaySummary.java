package com.fitbit.api.common.model.timeseries;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class IntradaySummary {

    private Data summary;
    private IntradayDataset intradayDataset;

    public IntradaySummary(JSONObject json, TimeSeriesResourceType resourceType) throws JSONException {
        String timeSeriesJsonName = resourceType.getResourcePath().substring(1).replace('/', '-');
        String intradayDataJsonName = timeSeriesJsonName + "-intraday";
        summary = Data.jsonArrayToDataList(json.getJSONArray(timeSeriesJsonName)).get(0);
        if (json.has(intradayDataJsonName)) {
            intradayDataset = new IntradayDataset(json.getJSONObject(intradayDataJsonName));
        }
    }

    public IntradaySummary(Data summary, IntradayDataset intradayDataset) {
        this.summary = summary;
        this.intradayDataset = intradayDataset;
    }

    public Data getSummary() {
        return summary;
    }

    public IntradayDataset getIntradayDataset() {
        return intradayDataset;
    }
}
