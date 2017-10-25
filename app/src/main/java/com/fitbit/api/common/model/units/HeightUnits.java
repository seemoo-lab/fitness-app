package com.fitbit.api.common.model.units;

public enum HeightUnits {
    CM("Centimeter"),
    INCHES("Inch");

    String unit;

    HeightUnits(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }
}