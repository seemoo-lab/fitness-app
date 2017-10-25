package com.fitbit.api.common.model.achievement;

import org.joda.time.LocalDate;

/**
 * Created by IntelliJ IDEA.
 * User: Kiryl
 * Date: 1/17/12
 * Time: 5:37 AM
 */
public class BestAchievementItem {

    private Double value;
    private LocalDate date;

    public BestAchievementItem(Double value, LocalDate date) {
        this.value = value;
        this.date = date;
    }

    public Double getValue() {
        return value;
    }

    public LocalDate getDate() {
        return date;
    }
}
