package com.timetracker.model;

import com.timetracker.util.TimeUtils;

public class SessionViewModel {

    private final int id;
    private final String startTime;
    private final String endTime;
    private final String categoryName;
    private final int durationMinutes;

    public SessionViewModel(int id, String startTime, String endTime, String categoryName, int durationMinutes) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.categoryName = categoryName;
        this.durationMinutes = durationMinutes;
    }

    public int id() {
        return id;
    }

    public String startTime() {
        return startTime;
    }

    public String endTime() {
        return endTime;
    }

    public String categoryName() {
        return categoryName;
    }

    public int durationMinutes() {
        return durationMinutes;
    }

    public String asDisplayString() {
        String durationText = TimeUtils.formatDuration(durationMinutes);
        return String.format("%s - %s %s (%s)", startTime, endTime, categoryName, durationText);
    }

    @Override
    public String toString() {
        return asDisplayString();
    }
}
