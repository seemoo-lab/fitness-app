package com.fitbit.api.common.model.units;

import java.util.Locale;

public enum UnitSystem {
    METRIC("METRIC", DurationUnits.MS, DistanceUnits.METRIC, HeightUnits.CM, WeightUnits.KG, MeasurementUnits.CM, VolumeUnits.ML),
    UK("en_GB", DurationUnits.MS, DistanceUnits.METRIC, HeightUnits.CM, WeightUnits.STONE, MeasurementUnits.CM, VolumeUnits.ML),
    US("en_US", DurationUnits.MS, DistanceUnits.US, HeightUnits.INCHES, WeightUnits.POUNDS, MeasurementUnits.INCHES, VolumeUnits.FL_OZ);

    String displayLocale;
    DurationUnits durationUnits;
    DistanceUnits distanceUnits;
    HeightUnits heightUnits;
    WeightUnits weightUnits;
    MeasurementUnits measurementUnits;
    VolumeUnits volumeUnits;

    UnitSystem(String displayLocale, DurationUnits durationUnits, DistanceUnits distanceUnits, HeightUnits heightUnits, WeightUnits weightUnits, MeasurementUnits measurementUnits, VolumeUnits volumeUnits) {
        this.displayLocale = displayLocale;
        this.durationUnits = durationUnits;
        this.distanceUnits = distanceUnits;
        this.heightUnits = heightUnits;
        this.weightUnits = weightUnits;
        this.measurementUnits = measurementUnits;
        this.volumeUnits = volumeUnits;
    }

    public String getDisplayLocale() {
        return displayLocale;
    }

    public DurationUnits getDurationUnits() {
        return durationUnits;
    }

    public DistanceUnits getDistanceUnits() {
        return distanceUnits;
    }

    public HeightUnits getHeightUnits() {
        return heightUnits;
    }

    public WeightUnits getWeightUnits() {
        return weightUnits;
    }

    public MeasurementUnits getMeasurementUnits() {
        return measurementUnits;
    }

    public VolumeUnits getVolumeUnits() {
        return volumeUnits;
    }

    public static UnitSystem getUnitSystem(Locale locale) {
        if (Locale.US.equals(locale)) {
            return US;
        } else if (Locale.UK.equals(locale)) {
            return UK;
        } else {
            return METRIC;
        }
    }

    public static UnitSystem findByDisplayLocale(String displayLocale) {
        for (UnitSystem unitSystem : values()) {
            if (unitSystem.displayLocale.equals(displayLocale)) {
                return unitSystem;
            }
        }
        return null;
    }
}
