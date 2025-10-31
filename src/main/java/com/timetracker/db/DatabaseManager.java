package com.timetracker.db;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager {

    private static final String DATABASE_NAME = "timetracker.db";
    private static final String JDBC_PREFIX = "jdbc:sqlite:";

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        ensureDriverLoaded();
        return DriverManager.getConnection(JDBC_PREFIX + getDatabasePath());
    }

    private static void ensureDriverLoaded() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
    }

    private static String getDatabasePath() {
        Path dbPath = Path.of(System.getProperty("user.dir"), DATABASE_NAME);
        return dbPath.toAbsolutePath().toString();
    }
}
