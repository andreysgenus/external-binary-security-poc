package com.poc.binarybroker;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

public class DateFormatUtils {

    public static final String ISODATETIME_GENERIC_PATTERN = "yyyy[-MM][-dd['T'HH[:mm[:ss[.SSS]]]]][XXX]";

    public static final DateTimeFormatter ISO_ROBUST_DATE_TIME = robustOfPattern(ISODATETIME_GENERIC_PATTERN);

    public static final DateTimeFormatter robustOfPattern(String pattern) {
        return new DateTimeFormatterBuilder().appendPattern(pattern)
                .parseDefaulting(MONTH_OF_YEAR, 1)
                .parseDefaulting(DAY_OF_MONTH, 1)
                .parseDefaulting(HOUR_OF_DAY, 0)
                .parseDefaulting(MINUTE_OF_HOUR, 0)
                .parseDefaulting(SECOND_OF_MINUTE, 0)
                .parseDefaulting(NANO_OF_SECOND, 0)
                .toFormatter()
                .withZone(ZoneOffset.UTC);
    }
    public static String formatISODateTime(Date date) {
        return formatISODateTime(toZonedDateTime(date));
    }

    public static String formatISODateTime(ZonedDateTime zdt) {
        if (zdt == null) {
            return null;
        }
        return ISO_ROBUST_DATE_TIME.format(zdt);
    }

    public static ZonedDateTime toZonedDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
    }
}
