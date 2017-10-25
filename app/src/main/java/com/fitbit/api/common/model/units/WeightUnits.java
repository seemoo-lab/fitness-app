package com.fitbit.api.common.model.units;

public enum WeightUnits {
    KG("kg"),
    POUNDS("lb"),
    STONE("stone");

    String text;

    WeightUnits(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
