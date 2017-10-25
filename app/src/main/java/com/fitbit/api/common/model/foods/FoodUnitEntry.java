package com.fitbit.api.common.model.foods;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * User: Dzmitry Krutko
 * Date: Jan 02, 2011
 * Time: 11:55:47 AM
 */
public class FoodUnitEntry {
    private final float servingSize;
    private final UUID foodMeasurementUnitUuid;
    private final float multiplier;
    private final boolean defaultUnit;

    public FoodUnitEntry(float servingSize, UUID foodMeasurementUnitUuid, float multiplier, boolean defaultUnit) {
        this.servingSize = servingSize;
        this.foodMeasurementUnitUuid = foodMeasurementUnitUuid;
        this.multiplier = multiplier;
        this.defaultUnit = defaultUnit;
    }

    public FoodUnitEntry(JSONObject json) throws JSONException {
        this.servingSize = (float) json.getDouble("servingSize");
        this.foodMeasurementUnitUuid = UUID.fromString(json.getString("foodMeasurementUnitUuid"));
        this.multiplier = (float) json.getDouble("multiplier");
        this.defaultUnit = json.getBoolean("isDefaultUnit");
    }

    public float getServingSize() {
        return servingSize;
    }

    public String getFoodMeasurementUnitUuid() {
        return foodMeasurementUnitUuid.toString();
    }

    public UUID foodMeasurementUnitUuid() {
        return foodMeasurementUnitUuid;
    }

    public boolean isDefaultUnit() {
        return defaultUnit;
    }

    public float getMultiplier() {
        return multiplier;
    }
}
