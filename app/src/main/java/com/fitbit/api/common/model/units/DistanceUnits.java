package com.fitbit.api.common.model.units;

public enum DistanceUnits {
    METRIC("km"),
    US("miles");

    String text;
    DistanceUnits(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

}