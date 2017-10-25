package com.fitbit.api.common.model.units;

public enum MeasurementUnits {
    CM("Centimeter"),
    INCHES("Inch");

    String unit;

    MeasurementUnits(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }
}