package com.timetracker.tracking;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ActivityAggregator {

    public List<ActivitySession> buildSessions(List<ActivityEvent> events, Instant closingBoundary) {
        if (events.isEmpty()) {
            return List.of();
        }
        List<ActivityEvent> sorted = new ArrayList<>(events);
        sorted.sort(Comparator.comparing(ActivityEvent::timestamp));

        List<ActivitySession> sessions = new ArrayList<>();
        SessionDraft current = null;

        for (ActivityEvent event : sorted) {
            Instant ts = event.timestamp();
            switch (event.type()) {
                case FOCUS, URL_CHANGE -> {
                    current = closeCurrent(current, ts, sessions);
                    current = SessionDraft.fromEvent(event, ActivitySessionSource.AUTO, false);
                }
                case MANUAL_START -> {
                    current = closeCurrent(current, ts, sessions);
                    current = SessionDraft.fromEvent(event, ActivitySessionSource.MANUAL, false);
                }
                case MANUAL_STOP -> {
                    if (current != null && current.source == ActivitySessionSource.MANUAL) {
                        current = closeCurrent(current, ts, sessions);
                    }
                }
                case IDLE_ON, LOCK -> current = closeCurrent(current, ts, sessions);
                case IDLE_OFF, UNLOCK -> {
                    // wait for the next focus/manual event
                }
            }
        }

        closeCurrent(current, closingBoundary, sessions);
        return sessions;
    }

    public List<ActivityDailyTotal> summarize(List<ActivitySession> sessions) {
        Map<TotalKey, Long> totals = new LinkedHashMap<>();

        for (ActivitySession session : sessions) {
            if (session.idle()) {
                continue;
            }
            long seconds = Math.max(0, session.durationSeconds());
            String domain = ActivityUrlUtils.extractDomain(session.url()).orElse(null);
            LocalDate date = LocalDate.ofInstant(session.start(), ZoneId.systemDefault());
            TotalKey key = new TotalKey(date, session.appId(), domain, session.url(), session.source());
            totals.merge(key, seconds, Long::sum);
        }

        List<ActivityDailyTotal> result = new ArrayList<>();
        for (Map.Entry<TotalKey, Long> entry : totals.entrySet()) {
            TotalKey key = entry.getKey();
            result.add(new ActivityDailyTotal(key.date, key.appId, key.domain, key.url, key.source, entry.getValue()));
        }
        return result;
    }

    private SessionDraft closeCurrent(SessionDraft current, Instant end, List<ActivitySession> sessions) {
        if (current == null || end == null) {
            return current;
        }
        if (end.isBefore(current.start)) {
            return null;
        }
        Duration duration = Duration.between(current.start, end);
        if (!duration.isNegative() && !duration.isZero()) {
            sessions.add(current.toSession(end));
        }
        return null;
    }

    private static final class SessionDraft {
        private final Instant start;
        private final String appId;
        private final String windowTitle;
        private final String url;
        private final ActivitySessionSource source;
        private final boolean idle;

        private SessionDraft(Instant start,
                             String appId,
                             String windowTitle,
                             String url,
                             ActivitySessionSource source,
                             boolean idle) {
            this.start = Objects.requireNonNull(start, "start");
            this.appId = appId;
            this.windowTitle = windowTitle;
            this.url = url;
            this.source = Objects.requireNonNull(source, "source");
            this.idle = idle;
        }

        private static SessionDraft fromEvent(ActivityEvent event, ActivitySessionSource source, boolean idle) {
            return new SessionDraft(event.timestamp(), event.appId(), event.windowTitle(), event.url(), source, idle);
        }

        private ActivitySession toSession(Instant end) {
            return new ActivitySession(start, end, appId, windowTitle, url, source, idle);
        }
    }

    private record TotalKey(LocalDate date,
                            String appId,
                            String domain,
                            String url,
                            ActivitySessionSource source) {
    }
}
