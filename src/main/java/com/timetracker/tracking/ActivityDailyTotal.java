package com.timetracker.tracking;

import java.time.LocalDate;
import java.util.Objects;

public final class ActivityDailyTotal {

    private final LocalDate date;
    private final String appId;
    private final String domain;
    private final String url;
    private final ActivitySessionSource source;
    private final long totalSeconds;

    public ActivityDailyTotal(LocalDate date,
                              String appId,
                              String domain,
                              String url,
                              ActivitySessionSource source,
                              long totalSeconds) {
        this.date = Objects.requireNonNull(date, "date");
        this.appId = appId;
        this.domain = domain;
        this.url = url;
        this.source = Objects.requireNonNull(source, "source");
        this.totalSeconds = Math.max(0, totalSeconds);
    }

    public LocalDate date() {
        return date;
    }

    public String appId() {
        return appId;
    }

    public String domain() {
        return domain;
    }

    public String url() {
        return url;
    }

    public ActivitySessionSource source() {
        return source;
    }

    public long totalSeconds() {
        return totalSeconds;
    }
}
