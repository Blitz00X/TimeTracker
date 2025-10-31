package com.timetracker.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TimeUtils {

    private static final DateTimeFormatter HH_MM_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private TimeUtils() {
    }

    public static int minutesBetween(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        long minutes = duration.toMinutes();
        return (int) Math.max(0, minutes);
    }

    public static String formatHHmm(LocalDateTime dateTime) {
        return dateTime.format(HH_MM_FORMATTER);
    }

    public static String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + " dk";
        }
        int hours = minutes / 60;
        int remaining = minutes % 60;
        if (remaining == 0) {
            return hours + " sa";
        }
        return hours + " sa " + remaining + " dk";
    }

    public static String formatHHmmss(long totalSeconds) {
        long nonNegative = Math.max(0, totalSeconds);
        long hours = nonNegative / 3600;
        long minutes = (nonNegative % 3600) / 60;
        long seconds = nonNegative % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
