package com.timetracker.tracking;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class ActivityAggregationJob {

    private final ActivityEventDao eventDao;
    private final ActivitySessionDao sessionDao;
    private final ActivityAggregator aggregator;

    public ActivityAggregationJob(ActivityEventDao eventDao,
                                  ActivitySessionDao sessionDao,
                                  ActivityAggregator aggregator) {
        this.eventDao = Objects.requireNonNull(eventDao, "eventDao");
        this.sessionDao = Objects.requireNonNull(sessionDao, "sessionDao");
        this.aggregator = Objects.requireNonNull(aggregator, "aggregator");
    }

    public ActivityAggregationResult aggregate(Instant fromInclusive, Instant toExclusive, boolean persist) {
        List<ActivityEvent> events = eventDao.findBetween(fromInclusive, toExclusive);
        List<ActivitySession> sessions = aggregator.buildSessions(events, toExclusive);
        List<ActivityDailyTotal> totals = aggregator.summarize(sessions);

        if (persist) {
            sessionDao.insertSessions(sessions);
            sessionDao.upsertDailyTotals(totals);
        }

        return new ActivityAggregationResult(events, sessions, totals);
    }
}
