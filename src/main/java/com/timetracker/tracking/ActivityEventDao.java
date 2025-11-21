package com.timetracker.tracking;

import com.timetracker.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityEventDao {

    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT.withLocale(Locale.ROOT);

    public void insert(ActivityEvent event) {
        String sql = """
                INSERT INTO activity_events (ts, event_type, app_id, window_title, url, payload_json)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ISO_INSTANT.format(event.timestamp()));
            statement.setString(2, event.type().name());
            statement.setString(3, event.appId());
            statement.setString(4, event.windowTitle());
            statement.setString(5, event.url());
            statement.setString(6, event.payloadJson());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert activity event", e);
        }
    }

    public List<ActivityEvent> findBetween(Instant fromInclusive, Instant toExclusive) {
        String sql = """
                SELECT id, ts, event_type, app_id, window_title, url, payload_json
                FROM activity_events
                WHERE ts >= ? AND ts < ?
                ORDER BY ts ASC
                """;
        List<ActivityEvent> results = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ISO_INSTANT.format(fromInclusive));
            statement.setString(2, ISO_INSTANT.format(toExclusive));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(toEvent(resultSet));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query activity events", e);
        }
    }

    private ActivityEvent toEvent(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        if (resultSet.wasNull()) {
            id = null;
        }
        Instant ts = Instant.parse(resultSet.getString("ts"));
        ActivityEventType type = ActivityEventType.valueOf(resultSet.getString("event_type"));
        String appId = resultSet.getString("app_id");
        String windowTitle = resultSet.getString("window_title");
        String url = resultSet.getString("url");
        String payload = resultSet.getString("payload_json");
        return new ActivityEvent(id, ts, type, appId, windowTitle, url, payload);
    }
}
