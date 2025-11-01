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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Map;
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
        return toViewModels(sessionDao.findSessionsForDate(LocalDate.now()));
    }

    public List<SessionViewModel> getSessionsForDateRange(LocalDate startDate, LocalDate endDate) {
        validateRange(startDate, endDate);
        return toViewModels(sessionDao.findSessionsForDateRange(startDate, endDate));
    }

    public Map<String, Long> getCategorySummaryForDateRange(LocalDate startDate, LocalDate endDate) {
        validateRange(startDate, endDate);
        List<SessionDto> sessions = sessionDao.findSessionsForDateRange(startDate, endDate);
        Map<Integer, String> categories = new LinkedHashMap<>();
        for (SessionDto session : sessions) {
            categories.putIfAbsent(session.getCategoryId(), session.getCategoryName());
        }
        Map<String, Long> summary = new LinkedHashMap<>();
        for (Map.Entry<Integer, String> entry : categories.entrySet()) {
            long minutes = sessionDao.findTotalDurationMinutesForDateRange(startDate, endDate, entry.getKey());
            summary.put(entry.getValue(), minutes);
        }
        return summary;
    }

    public boolean deleteSession(int sessionId) {
        if (sessionId <= 0) {
            throw new IllegalArgumentException("sessionId must be positive");
        }
        return sessionDao.deleteById(sessionId);
    }

    public Optional<SessionDto> findSessionById(int sessionId) {
        if (sessionId <= 0) {
            throw new IllegalArgumentException("sessionId must be positive");
        }
        return sessionDao.findById(sessionId);
    }

    public synchronized OptionalLong getRemainingSecondsForCategoryToday(Category category) {
        Objects.requireNonNull(category, "category");
        LocalDate today = LocalDate.now();
        SessionDao.UsageAdjustment adjustment = sessionDao.findUsageAdjustment(today, category.getId());
        long offsetSeconds = adjustment.offsetSeconds();
        Long overrideLimitSeconds = adjustment.overrideLimitSeconds();
        long totalSeconds = sessionDao.findTotalDurationSecondsForDateAndCategory(today, category.getId());
        long usedSeconds = Math.max(0, totalSeconds - offsetSeconds);
        if (activeSession != null && activeSession.category().getId() == category.getId()) {
            long elapsedSeconds = Duration.between(activeSession.startTime(), LocalDateTime.now()).getSeconds();
            usedSeconds += Math.max(0, elapsedSeconds);
        }
        if (overrideLimitSeconds != null) {
            if (overrideLimitSeconds < 0) {
                return OptionalLong.empty();
            }
            long remaining = Math.max(0, overrideLimitSeconds - usedSeconds);
            return OptionalLong.of(remaining);
        }
        Integer limitMinutes = category.getDailyLimitMinutes();
        if (limitMinutes == null) {
            return OptionalLong.empty();
        }
        long limitSeconds = limitMinutes * 60L;
        long remaining = Math.max(0, limitSeconds - usedSeconds);
        return OptionalLong.of(remaining);
    }

    public synchronized void resetUsageForToday(Category category) {
        Objects.requireNonNull(category, "category");
        if (category.getDailyLimitMinutes() == null) {
            throw new IllegalStateException("Category has no daily limit to reset");
        }
        LocalDate today = LocalDate.now();
        long totalSeconds = sessionDao.findTotalDurationSecondsForDateAndCategory(today, category.getId());
        if (activeSession != null && activeSession.category().getId() == category.getId()) {
            long elapsedSeconds = Duration.between(activeSession.startTime(), LocalDateTime.now()).getSeconds();
            totalSeconds += Math.max(0, elapsedSeconds);
            activeSession = null;
        }
        sessionDao.saveUsageAdjustment(today, category.getId(), totalSeconds, null);
    }

    public synchronized void setRemainingSecondsForToday(Category category, Long remainingSeconds) {
        Objects.requireNonNull(category, "category");
        if (activeSession != null && activeSession.category().getId() == category.getId()) {
            throw new IllegalStateException("Stop the active session before adjusting today's remaining time.");
        }

        LocalDate today = LocalDate.now();
        SessionDao.UsageAdjustment adjustment = sessionDao.findUsageAdjustment(today, category.getId());
        long offsetSeconds = adjustment.offsetSeconds();
        long totalSeconds = sessionDao.findTotalDurationSecondsForDateAndCategory(today, category.getId());
        long usedSeconds = Math.max(0, totalSeconds - offsetSeconds);
        long normalizedOffset = totalSeconds - usedSeconds;

        if (remainingSeconds == null) {
            sessionDao.saveUsageAdjustment(today, category.getId(), normalizedOffset, -1L);
            return;
        }

        if (remainingSeconds < 0) {
            throw new IllegalArgumentException("remainingSeconds must be non-negative");
        }

        long newLimit = usedSeconds + remainingSeconds;
        sessionDao.saveUsageAdjustment(today, category.getId(), normalizedOffset, newLimit);
    }

    public synchronized void deleteSessionsForCategory(int categoryId) {
        if (activeSession != null && activeSession.category().getId() == categoryId) {
            activeSession = null;
        }
        sessionDao.deleteByCategory(categoryId);
        sessionDao.deleteUsageResetsForCategory(categoryId);
    }

    public Session updateSession(int sessionId, LocalDateTime startTime, LocalDateTime endTime) {
        if (sessionId <= 0) {
            throw new IllegalArgumentException("sessionId must be positive");
        }
        Objects.requireNonNull(startTime, "startTime");
        Objects.requireNonNull(endTime, "endTime");
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
        SessionDto existing = sessionDao.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        int durationMinutes = TimeUtils.minutesBetween(startTime, endTime);
        Session updated = new Session(existing.getId(), existing.getCategoryId(), startTime, endTime, durationMinutes);
        return sessionDao.update(updated);
    }

    public String generateIcsForDateRange(LocalDate startDate, LocalDate endDate) {
        validateRange(startDate, endDate);
        List<SessionDto> sessions = sessionDao.findSessionsForDateRange(startDate, endDate);
        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        DateTimeFormatter stampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        String lineSep = "\r\n";
        StringBuilder builder = new StringBuilder();
        builder.append("BEGIN:VCALENDAR").append(lineSep)
                .append("VERSION:2.0").append(lineSep)
                .append("PRODID:-//TimeTracker+//EN").append(lineSep)
                .append("CALSCALE:GREGORIAN").append(lineSep)
                .append("METHOD:PUBLISH").append(lineSep);

        String dtStamp = LocalDateTime.now(ZoneOffset.UTC).format(stampFormatter);

        for (SessionDto session : sessions) {
            builder.append("BEGIN:VEVENT").append(lineSep);
            builder.append("UID:session-").append(session.getId()).append("@timetracker").append(lineSep);
            builder.append("DTSTAMP:").append(dtStamp).append(lineSep);
            builder.append("DTSTART:").append(session.getStartTime().format(dtFormatter)).append(lineSep);
            builder.append("DTEND:").append(session.getEndTime().format(dtFormatter)).append(lineSep);
            builder.append("SUMMARY:").append(escapeText(session.getCategoryName())).append(lineSep);
            builder.append("END:VEVENT").append(lineSep);
        }

        builder.append("END:VCALENDAR").append(lineSep);
        return builder.toString();
    }

    public String generateTodayIcs() {
        LocalDate today = LocalDate.now();
        return generateIcsForDateRange(today, today);
    }

    public String generateCsvForDateRange(LocalDate startDate, LocalDate endDate) {
        validateRange(startDate, endDate);
        List<SessionDto> sessions = sessionDao.findSessionsForDateRange(startDate, endDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String lineSep = System.lineSeparator();
        StringBuilder builder = new StringBuilder();
        builder.append("Category,Start Time,End Time,Duration (min)").append(lineSep);
        for (SessionDto session : sessions) {
            builder.append(escapeCsvValue(session.getCategoryName())).append(',')
                    .append(escapeCsvValue(formatter.format(session.getStartTime()))).append(',')
                    .append(escapeCsvValue(formatter.format(session.getEndTime()))).append(',')
                    .append(session.getDurationMinutes()).append(lineSep);
        }
        return builder.toString();
    }

    private String escapeText(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }

    private String escapeCsvValue(String value) {
        String safeValue = value == null ? "" : value;
        boolean needsQuoting = safeValue.contains(",") || safeValue.contains("\"")
                || safeValue.contains("\n") || safeValue.contains("\r");
        String escaped = safeValue.replace("\"", "\"\"");
        if (needsQuoting) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        Objects.requireNonNull(startDate, "startDate");
        Objects.requireNonNull(endDate, "endDate");
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be on or after startDate");
        }
    }

    private List<SessionViewModel> toViewModels(List<SessionDto> sessions) {
        return sessions.stream()
                .map(dto -> new SessionViewModel(
                        dto.getId(),
                        TimeUtils.formatHHmm(dto.getStartTime()),
                        TimeUtils.formatHHmm(dto.getEndTime()),
                        dto.getCategoryName(),
                        dto.getDurationMinutes()))
                .collect(Collectors.toList());
    }

    public record ActiveSession(Category category, LocalDateTime startTime, Long allowedSeconds) {
    }
}
