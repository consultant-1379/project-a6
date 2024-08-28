package com.ericsson.graduate.microservices.gerrit.json;

import java.time.format.DateTimeFormatter;

public class DateFormatter {
    private DateFormatter() {
    }

    private static final String DATE_PATTERN = "EEE MMM dd HH:mm:ss yyyy Z";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
}
