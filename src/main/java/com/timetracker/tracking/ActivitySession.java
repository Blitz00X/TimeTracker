package com.timetracker.tracking;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class ActivitySession {

    private final Long id;
    private final Instant start;
    private final Instant end;
    private final String appId;
    private final String windowTitle;
    private final String url;
    private final ActivitySessionSource source;
    private final boolean idle;

    public ActivitySession(Long id,
                           Instant start,
                           Instant end,
                           String appId,
                           String windowTitle,
                           String url,
                           ActivitySessionSource source,
                           boolean idle) {
        this.id = id;
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
        this.appId = appId;
        this.windowTitle = windowTitle;
        this.url = url;
        this.source = Objects.requireNonNull(source, "source");
        this.idle = idle;
    }

    public ActivitySession(Instant start,
                           Instant end,
                           String appId,
                           String windowTitle,
                           String url,
                           ActivitySessionSource source,
                           boolean idle) {
        this(null, start, end, appId, windowTitle, url, source, idle);
    }

    public Long id() {
        return id;
    }

    public Instant start() {
        return start;
    }

    public Instant end() {
        return end;
    }

    public String appId() {
        return appId;
    }

    public String windowTitle() {
        return windowTitle;
    }

    public String url() {
        return url;
    }

    public ActivitySessionSource source() {
        return source;
    }

    public boolean idle() {
        return idle;
    }

    public long durationSeconds() {
        return Math.max(0, Duration.between(start, end).getSeconds());
    }
}
