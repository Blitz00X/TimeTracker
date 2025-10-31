package com.timetracker.model;

public class Category {

    private final int id;
    private final String name;
    private final Integer dailyLimitMinutes;

    public Category(int id, String name, Integer dailyLimitMinutes) {
        this.id = id;
        this.name = name;
        this.dailyLimitMinutes = dailyLimitMinutes;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getDailyLimitMinutes() {
        return dailyLimitMinutes;
    }

    @Override
    public String toString() {
        if (dailyLimitMinutes == null) {
            return name;
        }
        return name + " (" + dailyLimitMinutes + " dk/day)";
    }
}
