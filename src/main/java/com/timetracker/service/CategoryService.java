package com.timetracker.service;

import com.timetracker.dao.CategoryDao;
import com.timetracker.model.Category;

import java.util.List;
import java.util.Objects;

public class CategoryService {

    private final CategoryDao categoryDao;

    public CategoryService() {
        this(new CategoryDao());
    }

    public CategoryService(CategoryDao categoryDao) {
        this.categoryDao = Objects.requireNonNull(categoryDao, "categoryDao");
    }

    public List<Category> getAllCategories() {
        return categoryDao.findAll();
    }

    public Category createCategory(String name, Integer dailyLimitMinutes) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        if (dailyLimitMinutes != null && dailyLimitMinutes <= 0) {
            throw new IllegalArgumentException("Daily limit must be positive");
        }
        return categoryDao.insert(name.trim(), dailyLimitMinutes);
    }

    public Category updateCategoryLimit(int categoryId, Integer dailyLimitMinutes) {
        if (dailyLimitMinutes != null && dailyLimitMinutes <= 0) {
            throw new IllegalArgumentException("Daily limit must be positive");
        }
        return categoryDao.updateDailyLimit(categoryId, dailyLimitMinutes);
    }
}
