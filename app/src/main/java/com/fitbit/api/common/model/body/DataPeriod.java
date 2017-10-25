package com.fitbit.api.common.model.body;

/**
 * Created by IntelliJ IDEA.
 * User: Kiryl
 * Date: 2/11/12
 * Time: 4:59 AM
 */
public enum DataPeriod {

    ONE_DAY("1d", 1),
    SEVEN_DAYS("7d", 7),
    THIRTY_DAYS("30d", 30),
    ONE_WEEK("1w", 7),
    ONE_MONTH("1m", 31);

    private String shortForm;
    private int days;

    private DataPeriod(String shortForm, int days) {
        this.shortForm = shortForm;
        this.days = days;
    }

    public static DataPeriod findByShortForm(String shortForm) {
        for (DataPeriod period : DataPeriod.values()) {
            if (period.shortForm.equals(shortForm)) {
                return period;
            }
        }
        return null;
    }

    public static DataPeriod findCeil(int days) {
        DataPeriod found = null;
        for (DataPeriod period : DataPeriod.values()) {
            if (period.getDays() >= days) {
                if (found == null || period.getDays() < found.getDays()) {
                    found = period;
                }
            }
        }
        return found;
    }

    public int getDays() {
        return days;
    }

    public String getShortForm() {
        return shortForm;
    }

}
