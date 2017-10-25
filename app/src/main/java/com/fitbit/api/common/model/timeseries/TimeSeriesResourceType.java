package com.fitbit.api.common.model.timeseries;

/**
 * User: gkutlu
 * Date: Aug 16, 2010
 * Time: 7:36:20 PM
 */
public enum TimeSeriesResourceType {
    //food
    CALORIES_IN("/foods/log/caloriesIn"),
    WATER("/foods/log/water"),
    //activity
    CALORIES_OUT("/activities/calories"),
    STEPS("/activities/steps"),
    DISTANCE("/activities/distance"),
    MINUTES_SEDENTARY("/activities/minutesSedentary"),
    MINUTES_LIGHTLY_ACTIVE("/activities/minutesLightlyActive"),
    MINUTES_FAIRLY_ACTIVE("/activities/minutesFairlyActive"),
    MINUTES_VERY_ACTIVE("/activities/minutesVeryActive"),
    ACTIVITY_CALORIES("/activities/activityCalories"),
    FLOORS("/activities/floors"),
    ELEVATION("/activities/elevation"),
    //tracker activity
    CALORIES_OUT_TRACKER("/activities/tracker/calories"),
    STEPS_TRACKER("/activities/tracker/steps"),
    DISTANCE_TRACKER("/activities/tracker/distance"),
    ACTIVITY_CALORIES_TRACKER("/activities/tracker/activityCalories"),
    FLOORS_TRACKER("/activities/tracker/floors"),
    ELEVATION_TRACKER("/activities/tracker/elevation"),
    MINUTES_SEDENTARY_TRACKER("/activities/tracker/minutesSedentary"),
    MINUTES_LIGHTLY_ACTIVE_TRACKER("/activities/tracker/minutesLightlyActive"),
    MINUTES_FAIRLY_ACTIVE_TRACKER("/activities/tracker/minutesFairlyActive"),
    MINUTES_VERY_ACTIVE_TRACKER("/activities/tracker/minutesVeryActive"),
    //sleep
    MINUTES_ASLEEP("/sleep/minutesAsleep"),
    MINUTES_AWAKE("/sleep/minutesAwake"),
    AWAKENINGS_COUNT("/sleep/awakeningsCount"),
    TIME_IN_BED("/sleep/timeInBed"),
    MINUTES_TO_FALL_ASLEEP("/sleep/minutesToFallAsleep"),
    MINUTES_AFTER_WAKEUP("/sleep/minutesAfterWakeup"),
    TIME_ENTERED_BED("/sleep/startTime"),
    EFFICIENCY("/sleep/efficiency"),
    //body
    WEIGHT("/body/weight"),
    BMI("/body/bmi"),
    FAT("/body/fat");

    private String resourcePath;

    TimeSeriesResourceType(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}