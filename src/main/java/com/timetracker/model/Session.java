package com.timetracker.model;

import java.time.LocalDateTime;

public class Session {

    private final int id;
    private final int categoryId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final int durationMinutes;

    public Session(int id, int categoryId, LocalDateTime startTime, LocalDateTime endTime, int durationMinutes) {
        this.id = id;
        this.categoryId = categoryId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
    }

    public int getId() {
        return id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
}
