package com.fitbit.api.common.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class FitbitApiService {
    protected static final Log log = LogFactory.getLog(FitbitApiService.class);

    public static final String LOCAL_DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
    public static final String TIME_UPDATED_PATTERN = DATE_TIME_PATTERN;
    public static final String LOCAL_TIME_HOURS_MINUTES_PATTERN = "HH:mm";
    public static final String LOCAL_TIME_HOURS_MINUTES_SECONDS_PATTERN = "HH:mm:ss";

    public static final DateTimeZone SERVER_TIME_ZONE = DateTimeZone.getDefault();
    public static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormat.forPattern(LOCAL_DATE_PATTERN).withZone(SERVER_TIME_ZONE);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern(DATE_TIME_PATTERN).withZone(SERVER_TIME_ZONE);
    public static final DateTimeFormatter TIME_UPDATED_FORMATTER = DateTimeFormat.forPattern(TIME_UPDATED_PATTERN).withZone(SERVER_TIME_ZONE);
    public static final DateTimeFormatter LOCAL_TIME_HOURS_MINUTES_FORMATTER = DateTimeFormat.forPattern(LOCAL_TIME_HOURS_MINUTES_PATTERN);
    public static final DateTimeFormatter LOCAL_TIME_HOURS_MINUTES_SECONDS_FORMATTER = DateTimeFormat.forPattern(LOCAL_TIME_HOURS_MINUTES_SECONDS_PATTERN);


    public static LocalDate getValidLocalDateOrNull(String date) {
        if (StringUtils.isEmpty(date)) return null;

        try {
            return FitbitApiService.LOCAL_DATE_FORMATTER.parseDateTime(date).toLocalDate();
        } catch (Exception e) {
            log.error(e);
        }

        return null;
    }

    public static DateTime getValidDateTimeOrNull(String date) {
        if (StringUtils.isEmpty(date)) return null;

        try {
            return FitbitApiService.DATE_TIME_FORMATTER.parseDateTime(date);
        } catch (Exception e) {
            log.error(e);
        }

        return null;
    }

    public static DateTime getValidTimeUpdatedOrNull(String date) {
        if (StringUtils.isEmpty(date)) return null;

        try {
            return FitbitApiService.TIME_UPDATED_FORMATTER.parseDateTime(date);
        } catch (Exception e) {
            log.error(e);
        }

        return null;
    }

    public static DateTime roundToSeconds(DateTime dateTime) {
        return dateTime.minusMillis(dateTime.getMillisOfSecond());
    }

    public static LocalTime getValidTimeOrNull(String date) {
        if (StringUtils.isEmpty(date)) return null;

        try {
            return FitbitApiService.LOCAL_TIME_HOURS_MINUTES_FORMATTER.parseDateTime(date).toLocalTime();
        } catch (Exception e) {
            log.error(e);
        }

        return null;
   }
}
