package com.timetracker.tracking;

import java.time.Instant;
import java.util.Objects;

public final class ActivityEvent {

    private final Long id;
    private final Instant timestamp;
    private final ActivityEventType type;
    private final String appId;
    private final String windowTitle;
    private final String url;
    private final String payloadJson;

    public ActivityEvent(Long id,
                         Instant timestamp,
                         ActivityEventType type,
                         String appId,
                         String windowTitle,
                         String url,
                         String payloadJson) {
        this.id = id;
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.type = Objects.requireNonNull(type, "type");
        this.appId = appId;
        this.windowTitle = windowTitle;
        this.url = url;
        this.payloadJson = payloadJson;
    }

    public ActivityEvent(Instant timestamp,
                         ActivityEventType type,
                         String appId,
                         String windowTitle,
                         String url,
                         String payloadJson) {
        this(null, timestamp, type, appId, windowTitle, url, payloadJson);
    }

    public Long id() {
        return id;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public ActivityEventType type() {
        return type;
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

    public String payloadJson() {
        return payloadJson;
    }
}
