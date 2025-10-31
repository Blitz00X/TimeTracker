package com.timetracker.service;

import com.timetracker.dao.SessionDao;
import com.timetracker.model.Category;
import com.timetracker.model.Session;
import com.timetracker.model.SessionDto;
import com.timetracker.model.SessionViewModel;
import com.timetracker.util.TimeUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;

public class SessionService {

    private final SessionDao sessionDao;
    private ActiveSession activeSession;

    public SessionService() {
        this(new SessionDao());
    }

    public SessionService(SessionDao sessionDao) {
        this.sessionDao = Objects.requireNonNull(sessionDao, "sessionDao");
    }

    public synchronized void startSession(Category category, Long allowedSeconds) {
        if (category == null) {
            throw new IllegalArgumentException("Category must be provided to start a session");
        }
        if (activeSession != null) {
            throw new IllegalStateException("A session is already running");
        }
        if (allowedSeconds != null && allowedSeconds <= 0) {
            throw new IllegalArgumentException("allowedSeconds must be positive when provided");
        }
        activeSession = new ActiveSession(category, LocalDateTime.now(), allowedSeconds);
    }

    public synchronized Optional<Session> stopSession() {
        if (activeSession == null) {
            return Optional.empty();
        }
        LocalDateTime endTime = LocalDateTime.now();
        Long allowedSeconds = activeSession.allowedSeconds();
        if (allowedSeconds != null) {
            LocalDateTime limitEndTime = activeSession.startTime().plusSeconds(allowedSeconds);
            if (endTime.isAfter(limitEndTime)) {
                endTime = limitEndTime;
            }
        }
        int durationMinutes = TimeUtils.minutesBetween(activeSession.startTime(), endTime);
        Session toSave = new Session(
                0,
                activeSession.category().getId(),
                activeSession.startTime(),
                endTime,
                durationMinutes
        );
        Session persisted = sessionDao.insert(toSave);
        activeSession = null;
        return Optional.of(persisted);
    }

    public synchronized void cancelActiveSession() {
        activeSession = null;
    }

    public synchronized boolean isSessionRunning() {
        return activeSession != null;
    }

    public synchronized Optional<ActiveSession> getActiveSession() {
        return Optional.ofNullable(activeSession);
    }

    public List<SessionViewModel> getTodaySessions() {
        List<SessionDto> sessions = sessionDao.findSessionsForDate(LocalDate.now());
        return sessions.stream()
                .map(dto -> new SessionViewModel(
                        dto.getId(),
                        TimeUtils.formatHHmm(dto.getStartTime()),
                        TimeUtils.formatHHmm(dto.getEndTime()),
                        dto.getCategoryName(),
                        dto.getDurationMinutes()))
                .collect(Collectors.toList());
    }

    public boolean deleteSession(int sessionId) {
        if (sessionId <= 0) {
            throw new IllegalArgumentException("sessionId must be positive");
        }
        return sessionDao.deleteById(sessionId);
    }

    public synchronized OptionalLong getRemainingSecondsForCategoryToday(Category category) {
        Objects.requireNonNull(category, "category");
        Integer limitMinutes = category.getDailyLimitMinutes();
        if (limitMinutes == null) {
            return OptionalLong.empty();
        }
        long limitSeconds = limitMinutes * 60L;
        long usedSeconds = sessionDao.findTotalDurationSecondsForDateAndCategory(LocalDate.now(), category.getId());
        if (activeSession != null && activeSession.category().getId() == category.getId()) {
            long elapsedSeconds = Duration.between(activeSession.startTime(), LocalDateTime.now()).getSeconds();
            usedSeconds += Math.max(0, elapsedSeconds);
        }
        long remaining = Math.max(0, limitSeconds - usedSeconds);
        return OptionalLong.of(remaining);
    }

    public record ActiveSession(Category category, LocalDateTime startTime, Long allowedSeconds) {
    }
}
