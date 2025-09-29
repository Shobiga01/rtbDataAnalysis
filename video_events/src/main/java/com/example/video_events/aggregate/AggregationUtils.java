package com.example.video_events.aggregate;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


public final class AggregationUtils {
    private AggregationUtils() {}


    public static String buildDailyIndexName(String prefix, Instant now) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy.MM.dd").withZone(ZoneOffset.UTC);
        return String.format("%s-%s", prefix, fmt.format(now));
    }
}