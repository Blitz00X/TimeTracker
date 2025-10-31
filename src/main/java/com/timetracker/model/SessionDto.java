package com.timetracker.model;

import java.time.LocalDateTime;

public class SessionDto {

    private final int id;
    private final int categoryId;
    private final String categoryName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final int durationMinutes;

    public SessionDto(int id, int categoryId, String categoryName,
                      LocalDateTime startTime, LocalDateTime endTime, int durationMinutes) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
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

    public String getCategoryName() {
        return categoryName;
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
