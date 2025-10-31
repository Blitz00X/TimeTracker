package com.timetracker.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    private static final String CREATE_CATEGORIES_SQL = """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                daily_limit_minutes INTEGER
            );
            """;

    private static final String CREATE_SESSIONS_SQL = """
            CREATE TABLE IF NOT EXISTS sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category_id INTEGER NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT NOT NULL,
                duration_minutes INTEGER NOT NULL,
                FOREIGN KEY(category_id) REFERENCES categories(id)
            );
            """;

    private static final String ALTER_CATEGORIES_ADD_LIMIT_SQL = """
            ALTER TABLE categories ADD COLUMN daily_limit_minutes INTEGER
            """;

    private DatabaseInitializer() {
    }

    public static void initialize() {
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(CREATE_CATEGORIES_SQL);
            statement.execute(CREATE_SESSIONS_SQL);
            ensureDailyLimitColumn(statement);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }

    private static void ensureDailyLimitColumn(Statement statement) throws SQLException {
        try {
            statement.execute(ALTER_CATEGORIES_ADD_LIMIT_SQL);
        } catch (SQLException e) {
            String message = e.getMessage();
            if (message == null || !message.toLowerCase().contains("duplicate column")) {
                throw e;
            }
        }
    }
}
