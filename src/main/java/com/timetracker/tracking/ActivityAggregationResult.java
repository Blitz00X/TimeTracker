package com.timetracker.tracking;

import java.util.List;

public record ActivityAggregationResult(List<ActivityEvent> events,
                                        List<ActivitySession> sessions,
                                        List<ActivityDailyTotal> dailyTotals) {
}
