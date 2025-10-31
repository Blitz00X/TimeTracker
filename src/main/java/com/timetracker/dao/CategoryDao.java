package com.timetracker.dao;

import com.timetracker.db.DatabaseManager;
import com.timetracker.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {

    private static final String SELECT_ALL_SQL = "SELECT id, name, daily_limit_minutes FROM categories ORDER BY name COLLATE NOCASE";
    private static final String INSERT_SQL = "INSERT INTO categories(name, daily_limit_minutes) VALUES (?, ?)";
    private static final String UPDATE_LIMIT_SQL = "UPDATE categories SET daily_limit_minutes = ? WHERE id = ?";
    private static final String SELECT_BY_ID_SQL = "SELECT id, name, daily_limit_minutes FROM categories WHERE id = ?";

    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                categories.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load categories", e);
        }
        return categories;
    }

    public Category insert(String name, Integer dailyLimitMinutes) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name.trim());
            if (dailyLimitMinutes == null) {
                statement.setNull(2, Types.INTEGER);
            } else {
                statement.setInt(2, dailyLimitMinutes);
            }
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserting category failed, no rows affected.");
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return new Category(id, name.trim(), dailyLimitMinutes);
                }
                throw new SQLException("Inserting category failed, no ID obtained.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert category", e);
        }
    }

    public Category updateDailyLimit(int categoryId, Integer dailyLimitMinutes) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_LIMIT_SQL)) {
            if (dailyLimitMinutes == null) {
                statement.setNull(1, Types.INTEGER);
            } else {
                statement.setInt(1, dailyLimitMinutes);
            }
            statement.setInt(2, categoryId);
            int affected = statement.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Updating category failed, no rows affected.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update category limit", e);
        }
        return findById(categoryId);
    }

    public Category findById(int categoryId) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            statement.setInt(1, categoryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load category with id " + categoryId, e);
        }
        throw new IllegalStateException("Category with id " + categoryId + " not found");
    }

    private Category mapRow(ResultSet resultSet) throws SQLException {
        Object limitObj = resultSet.getObject("daily_limit_minutes");
        Integer limit = limitObj == null ? null : ((Number) limitObj).intValue();
        return new Category(resultSet.getInt("id"), resultSet.getString("name"), limit);
    }
}
