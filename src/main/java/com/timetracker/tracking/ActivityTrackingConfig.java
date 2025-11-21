package com.timetracker.tracking;

import java.time.Duration;
import java.util.Objects;

public final class ActivityTrackingConfig {

    private final Duration pollingInterval;
    private final Duration idleThreshold;
    private final boolean captureUrls;
    private final boolean redactQueryStrings;

    public ActivityTrackingConfig(Duration pollingInterval,
                                  Duration idleThreshold,
                                  boolean captureUrls,
                                  boolean redactQueryStrings) {
        this.pollingInterval = Objects.requireNonNull(pollingInterval, "pollingInterval");
        this.idleThreshold = Objects.requireNonNull(idleThreshold, "idleThreshold");
        this.captureUrls = captureUrls;
        this.redactQueryStrings = redactQueryStrings;
    }

    public Duration pollingInterval() {
        return pollingInterval;
    }

    public Duration idleThreshold() {
        return idleThreshold;
    }

    public boolean captureUrls() {
        return captureUrls;
    }

    public boolean redactQueryStrings() {
        return redactQueryStrings;
    }
}
