package com.timetracker.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ActivityTrackingService implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityTrackingService.class);

    private final ActiveAppCollector collector;
    private final ActivityEventDao eventDao;
    private final ActivityTrackingConfig config;
    private final ScheduledExecutorService executor;
    private volatile boolean paused;

    public ActivityTrackingService(ActiveAppCollector collector,
                                   ActivityEventDao eventDao,
                                   ActivityTrackingConfig config) {
        this.collector = Objects.requireNonNull(collector, "collector");
        this.eventDao = Objects.requireNonNull(eventDao, "eventDao");
        this.config = Objects.requireNonNull(config, "config");
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "activity-tracker");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        executor.scheduleAtFixedRate(this::captureSnapshot, 0L, config.pollingInterval().toMillis(), TimeUnit.MILLISECONDS);
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    private void captureSnapshot() {
        try {
            if (paused) {
                return;
            }
            Optional<ActiveAppSnapshot> snapshot = collector.capture();
            snapshot.ifPresent(value -> {
                String url = config.captureUrls()
                        ? ActivityUrlUtils.redactQuery(value.url(), config.redactQueryStrings()).orElse(null)
                        : null;
                ActivityEvent event = new ActivityEvent(
                        value.capturedAt(),
                        ActivityEventType.FOCUS,
                        value.appId(),
                        value.windowTitle(),
                        url,
                        null
                );
                eventDao.insert(event);
            });
        } catch (Exception e) {
            LOGGER.warn("Active app capture failed", e);
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
