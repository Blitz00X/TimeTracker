package com.timetracker.dao;

import com.timetracker.db.DatabaseManager;
import com.timetracker.model.Session;
import com.timetracker.model.SessionDto;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionDao {

    private static final String INSERT_SQL = """
            INSERT INTO sessions (category_id, start_time, end_time, duration_minutes)
            VALUES (?, ?, ?, ?)
            """;

    private static final String SELECT_FOR_DATE_SQL = """
            SELECT s.id,
                   s.category_id,
                   s.start_time,
                   s.end_time,
                   s.duration_minutes,
                   c.name AS category_name
            FROM sessions s
            INNER JOIN categories c ON c.id = s.category_id
            WHERE DATE(s.start_time) = ?
            ORDER BY s.start_time ASC
            """;

    private static final String DELETE_SQL = """
            DELETE FROM sessions
            WHERE id = ?
            """;

    private static final String SELECT_FOR_TOTAL_SECONDS_SQL = """
            SELECT start_time, end_time
            FROM sessions
            WHERE category_id = ?
              AND DATE(start_time) = ?
            """;

    private static final String DELETE_BY_CATEGORY_SQL = """
            DELETE FROM sessions
            WHERE category_id = ?
            """;

    private static final String UPSERT_USAGE_RESET_SQL = """
            INSERT INTO category_usage_resets (category_id, usage_date, offset_seconds, override_limit_seconds)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(category_id, usage_date)
            DO UPDATE SET offset_seconds = excluded.offset_seconds,
                          override_limit_seconds = excluded.override_limit_seconds
            """;

    private static final String SELECT_USAGE_RESET_SQL = """
            SELECT offset_seconds
            FROM category_usage_resets
            WHERE category_id = ?
              AND usage_date = ?
            """;

    private static final String DELETE_USAGE_RESETS_FOR_CATEGORY_SQL = """
            DELETE FROM category_usage_resets
            WHERE category_id = ?
            """;

    public Session insert(Session session) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, session.getCategoryId());
            statement.setString(2, session.getStartTime().toString());
            statement.setString(3, session.getEndTime().toString());
            statement.setInt(4, session.getDurationMinutes());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserting session failed, no rows affected.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Session(keys.getInt(1), session.getCategoryId(), session.getStartTime(),
                            session.getEndTime(), session.getDurationMinutes());
                }
            }
            throw new SQLException("Inserting session failed, no ID obtained.");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert session", e);
        }
    }

    public boolean deleteById(int sessionId) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, sessionId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete session with id " + sessionId, e);
        }
    }

    public void deleteByCategory(int categoryId) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_BY_CATEGORY_SQL)) {
            statement.setInt(1, categoryId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete sessions for category " + categoryId, e);
        }
    }

    public List<SessionDto> findSessionsForDate(LocalDate date) {
        List<SessionDto> sessions = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_FOR_DATE_SQL)) {
            statement.setString(1, date.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    sessions.add(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to fetch sessions for date " + date, e);
        }
        return sessions;
    }

    public long findTotalDurationSecondsForDateAndCategory(LocalDate date, int categoryId) {
        long totalSeconds = 0;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_FOR_TOTAL_SECONDS_SQL)) {
            statement.setInt(1, categoryId);
            statement.setString(2, date.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    do {
                        LocalDateTime start = LocalDateTime.parse(resultSet.getString("start_time"));
                        LocalDateTime end = LocalDateTime.parse(resultSet.getString("end_time"));
                        long seconds = Duration.between(start, end).getSeconds();
                        totalSeconds += Math.max(0, seconds);
                    } while (resultSet.next());
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to fetch total duration for category " + categoryId, e);
        }
        return totalSeconds;
    }

    public void saveUsageAdjustment(LocalDate date, int categoryId, long offsetSeconds, Long overrideLimitSeconds) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPSERT_USAGE_RESET_SQL)) {
            statement.setInt(1, categoryId);
            statement.setString(2, date.toString());
            statement.setLong(3, offsetSeconds);
            if (overrideLimitSeconds == null) {
                statement.setNull(4, Types.INTEGER);
            } else {
                statement.setLong(4, overrideLimitSeconds);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save usage offset for category " + categoryId, e);
        }
    }

    public UsageAdjustment findUsageAdjustment(LocalDate date, int categoryId) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USAGE_RESET_SQL)) {
            statement.setInt(1, categoryId);
            statement.setString(2, date.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    long offset = resultSet.getLong("offset_seconds");
                    Long override = null;
                    try {
                        Object overrideObj = resultSet.getObject("override_limit_seconds");
                        if (overrideObj != null) {
                            override = ((Number) overrideObj).longValue();
                        }
                    } catch (SQLException ignored) {
                        // Older database without override column; treat as null override.
                    }
                    return new UsageAdjustment(offset, override);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to fetch usage offset for category " + categoryId, e);
        }
        return new UsageAdjustment(0, null);
    }

    public void deleteUsageResetsForCategory(int categoryId) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_USAGE_RESETS_FOR_CATEGORY_SQL)) {
            statement.setInt(1, categoryId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete usage resets for category " + categoryId, e);
        }
    }

    public record UsageAdjustment(long offsetSeconds, Long overrideLimitSeconds) {
    }

    private SessionDto mapRow(ResultSet rs) throws SQLException {
        LocalDateTime start = LocalDateTime.parse(rs.getString("start_time"));
        LocalDateTime end = LocalDateTime.parse(rs.getString("end_time"));
        return new SessionDto(
                rs.getInt("id"),
                rs.getInt("category_id"),
                rs.getString("category_name"),
                start,
                end,
                rs.getInt("duration_minutes")
        );
    }
}
