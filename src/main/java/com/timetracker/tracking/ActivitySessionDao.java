package com.timetracker.tracking;

import com.timetracker.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ActivitySessionDao {

    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT.withLocale(Locale.ROOT);

    public void insertSessions(List<ActivitySession> sessions) {
        if (sessions.isEmpty()) {
            return;
        }
        String sql = """
                INSERT INTO activity_sessions (start_ts, end_ts, app_id, window_title, url, source, is_idle)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(start_ts, app_id, url, source, is_idle)
                DO UPDATE SET end_ts = excluded.end_ts,
                              window_title = excluded.window_title
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (ActivitySession session : sessions) {
                statement.setString(1, ISO_INSTANT.format(session.start()));
                statement.setString(2, ISO_INSTANT.format(session.end()));
                statement.setString(3, session.appId());
                statement.setString(4, session.windowTitle());
                statement.setString(5, session.url());
                statement.setString(6, session.source().name());
                statement.setInt(7, session.idle() ? 1 : 0);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert activity sessions", e);
        }
    }

    public void upsertDailyTotals(List<ActivityDailyTotal> totals) {
        if (totals.isEmpty()) {
            return;
        }
        String sql = """
                INSERT INTO activity_daily_totals (usage_date, app_id, domain, url, total_seconds, source)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT(usage_date, app_id, domain, url, source)
                DO UPDATE SET total_seconds = excluded.total_seconds
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (ActivityDailyTotal total : totals) {
                statement.setString(1, total.date().toString());
                statement.setString(2, total.appId());
                statement.setString(3, total.domain());
                statement.setString(4, total.url());
                statement.setLong(5, total.totalSeconds());
                statement.setString(6, total.source().name());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to upsert activity daily totals", e);
        }
    }

    public List<ActivityDailyTotal> findTotalsForDate(java.time.LocalDate date) {
        String sql = """
                SELECT usage_date, app_id, domain, url, total_seconds, source
                FROM activity_daily_totals
                WHERE usage_date = ?
                ORDER BY total_seconds DESC
                """;
        List<ActivityDailyTotal> results = new java.util.ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, date.toString());
            try (java.sql.ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(new ActivityDailyTotal(
                            java.time.LocalDate.parse(resultSet.getString("usage_date")),
                            resultSet.getString("app_id"),
                            resultSet.getString("domain"),
                            resultSet.getString("url"),
                            ActivitySessionSource.valueOf(resultSet.getString("source")),
                            resultSet.getLong("total_seconds")
                    ));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query activity daily totals", e);
        }
    }
}
