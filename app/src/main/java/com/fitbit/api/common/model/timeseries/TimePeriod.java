package com.fitbit.api.common.model.timeseries;

public enum TimePeriod {
	INTRADAY("1d", 1, "H:N:S"),
	SEVEN_DAYS("7d", 7, "MMM D"),
    THIRTY_DAYS("30d", 30, "MMM D"),
    ONE_WEEK("1w", 7, "MMM D"),
	ONE_MONTH("1m", 31, "MMM D"),
	THREE_MONTHS("3m", 93, "MMM D"),
	SIX_MONTHS("6m", 186, "MMM D"),
	ONE_YEAR("1y", 365, "MMM D"),
	MAX("max", 1095, "MMM YYYY");

	private String shortForm;
	private int days;
	private String dateFormat;

	private TimePeriod(String shortForm, int days, String format) {
		this.shortForm = shortForm;
		this.days = days;
		dateFormat = format;
	}

	public static TimePeriod findByShortForm(String shortForm) {
		for (TimePeriod period : TimePeriod.values()) {
			if (period.shortForm.equals(shortForm)) {
				return period;
			}
		}
		return null;
	}

	public static TimePeriod findCeil(int days) {
		TimePeriod found = null;
		for (TimePeriod period : TimePeriod.values()) {
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

	public String getDateFormat() {
		return dateFormat;
	}

    public String getShortForm() {
        return shortForm;
    }
}