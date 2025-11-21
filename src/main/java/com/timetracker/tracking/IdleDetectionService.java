package com.timetracker.tracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Emits IDLE_ON/IDLE_OFF events based on global input activity.
 */
public class IdleDetectionService implements AutoCloseable, NativeKeyListener, NativeMouseInputListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdleDetectionService.class);

    private final ActivityEventDao eventDao;
    private final Duration idleThreshold;
    private final ScheduledExecutorService scheduler;

    private volatile Instant lastActivity = Instant.now();
    private volatile boolean idle = false;

    public IdleDetectionService(ActivityEventDao eventDao, Duration idleThreshold) {
        this.eventDao = Objects.requireNonNull(eventDao, "eventDao");
        this.idleThreshold = Objects.requireNonNull(idleThreshold, "idleThreshold");
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "idle-detector");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        suppressJNativeHookLogging();
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            GlobalScreen.addNativeMouseListener(this);
            GlobalScreen.addNativeMouseMotionListener(this);
        } catch (NativeHookException e) {
            LOGGER.warn("Failed to register native hook for idle detection", e);
            return;
        }
        scheduler.scheduleAtFixedRate(this::checkIdle, 1, 1, TimeUnit.SECONDS);
    }

    private void checkIdle() {
        Instant now = Instant.now();
        Duration sinceActivity = Duration.between(lastActivity, now);
        if (!idle && sinceActivity.compareTo(idleThreshold) >= 0) {
            idle = true;
            eventDao.insert(new ActivityEvent(now, ActivityEventType.IDLE_ON, null, null, null, null));
        } else if (idle && sinceActivity.compareTo(idleThreshold) < 0) {
            idle = false;
            eventDao.insert(new ActivityEvent(now, ActivityEventType.IDLE_OFF, null, null, null, null));
        }
    }

    private void markActivity() {
        lastActivity = Instant.now();
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        markActivity();
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
        markActivity();
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
        markActivity();
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {
        markActivity();
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {
        markActivity();
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {
        markActivity();
    }

    @Override
    public void close() {
        scheduler.shutdownNow();
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            LOGGER.debug("Failed to unregister native hook cleanly", e);
        }
    }

    private void suppressJNativeHookLogging() {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);
    }
}
