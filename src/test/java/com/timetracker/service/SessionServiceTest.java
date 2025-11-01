package com.timetracker.service;

import com.timetracker.dao.SessionDao;
import com.timetracker.model.Category;
import com.timetracker.model.Session;
import com.timetracker.model.SessionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionDao sessionDao;

    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionService(sessionDao);
    }

    @Test
    void startSession_success_noLimit() {
        Category category = new Category(1, "Focus", null);

        sessionService.startSession(category, null);

        assertTrue(sessionService.isSessionRunning());
        SessionService.ActiveSession activeSession = sessionService.getActiveSession().orElseThrow();
        assertEquals(category, activeSession.category());
        assertNull(activeSession.allowedSeconds());
    }

    @Test
    void stopSession_success_withLimit() throws Exception {
        Category category = new Category(7, "Study", null);
        LocalDateTime start = LocalDateTime.now().minusMinutes(10);
        long allowedSeconds = 120L;

        Field activeSessionField = SessionService.class.getDeclaredField("activeSession");
        activeSessionField.setAccessible(true);
        activeSessionField.set(sessionService, new SessionService.ActiveSession(category, start, allowedSeconds));

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        when(sessionDao.insert(captor.capture())).thenAnswer(invocation -> {
            Session toSave = invocation.getArgument(0);
            return new Session(100, toSave.getCategoryId(), toSave.getStartTime(), toSave.getEndTime(), toSave.getDurationMinutes());
        });

        Optional<Session> result = sessionService.stopSession();

        assertTrue(result.isPresent());
        Session persisted = captor.getValue();
        LocalDateTime expectedEnd = start.plusSeconds(allowedSeconds);
        assertEquals(expectedEnd, persisted.getEndTime());
        assertEquals(start, persisted.getStartTime());
        assertEquals(2, persisted.getDurationMinutes());
        assertFalse(sessionService.isSessionRunning());
    }

    @Test
    void getRemainingSecondsForCategoryToday_noLimit() {
        Category category = new Category(3, "Read", null);
        LocalDate today = LocalDate.now();

        when(sessionDao.findUsageAdjustment(eq(today), eq(category.getId())))
                .thenReturn(new SessionDao.UsageAdjustment(0, null));
        when(sessionDao.findTotalDurationSecondsForDateAndCategory(eq(today), eq(category.getId())))
                .thenReturn(0L);

        OptionalLong remaining = sessionService.getRemainingSecondsForCategoryToday(category);

        assertTrue(remaining.isEmpty());
    }

    @Test
    void getRemainingSecondsForCategoryToday_withLimitAndAdjustment() {
        Category category = new Category(4, "Code", 120);
        LocalDate today = LocalDate.now();

        when(sessionDao.findUsageAdjustment(eq(today), eq(category.getId())))
                .thenReturn(new SessionDao.UsageAdjustment(600, null));
        when(sessionDao.findTotalDurationSecondsForDateAndCategory(eq(today), eq(category.getId())))
                .thenReturn(5400L);

        OptionalLong remaining = sessionService.getRemainingSecondsForCategoryToday(category);

        assertTrue(remaining.isPresent());
        assertEquals(2400L, remaining.getAsLong());
    }

    @Test
    void updateSession_updatesDurationCorrectly() {
        int sessionId = 11;
        LocalDateTime originalStart = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime originalEnd = originalStart.plusMinutes(30);
        SessionDto existing = new SessionDto(sessionId, 5, "Planning", originalStart, originalEnd, 30);
        when(sessionDao.findById(sessionId)).thenReturn(Optional.of(existing));

        ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
        when(sessionDao.update(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime newStart = originalStart.minusMinutes(15);
        LocalDateTime newEnd = originalStart.plusMinutes(45);

        Session updated = sessionService.updateSession(sessionId, newStart, newEnd);

        Session persisted = captor.getValue();
        assertEquals(newStart, persisted.getStartTime());
        assertEquals(newEnd, persisted.getEndTime());
        assertEquals(60, persisted.getDurationMinutes());
        assertEquals(60, updated.getDurationMinutes());
        verify(sessionDao).update(any(Session.class));
    }
}
