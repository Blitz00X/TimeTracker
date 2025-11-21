package com.timetracker.tracking;

import java.time.Instant;
import java.util.Objects;

public final class ActiveAppSnapshot {

    private final String appId;
    private final String windowTitle;
    private final String url;
    private final Instant capturedAt;

    public ActiveAppSnapshot(String appId, String windowTitle, String url, Instant capturedAt) {
        this.appId = appId;
        this.windowTitle = windowTitle;
        this.url = url;
        this.capturedAt = Objects.requireNonNull(capturedAt, "capturedAt");
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

    public Instant capturedAt() {
        return capturedAt;
    }
}
