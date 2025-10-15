package com.fa25se225.capstone.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    public static String formatInstant(Instant instant, ZoneId zoneId, String pattern) {
        if (instant == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern)
                .withZone(zoneId);
        return formatter.format(instant);
    }
}
