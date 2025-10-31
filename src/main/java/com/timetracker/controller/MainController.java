package com.timetracker.controller;

import com.timetracker.model.Category;
import com.timetracker.model.Session;
import com.timetracker.model.SessionViewModel;
import com.timetracker.service.CategoryService;
import com.timetracker.service.SessionService;
import com.timetracker.util.TimeUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.OptionalLong;

public class MainController {

    @FXML
    private ListView<Category> categoryListView;

    @FXML
    private ListView<SessionViewModel> timelineListView;

    @FXML
    private Label timerLabel;

    @FXML
    private Label selectedCategoryLabel;

    @FXML
    private Label remainingTimeLabel;

    @FXML
    private Label startTimeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button startStopButton;

    @FXML
    private Button resetButton;

    @FXML
    private Button addCategoryButton;

    private final CategoryService categoryService = new CategoryService();
    private final SessionService sessionService = new SessionService();

    private final ObservableList<Category> categoryItems = FXCollections.observableArrayList();
    private final ObservableList<SessionViewModel> timelineItems = FXCollections.observableArrayList();

    private Timeline tickingTimeline;

    @FXML
    private void initialize() {
        categoryListView.setItems(categoryItems);
        categoryListView.setPlaceholder(new Label("No categories yet"));
        categoryListView.setCellFactory(listView -> {
            MenuItem setLimitItem = new MenuItem("Set Daily Limit...");
            MenuItem adjustRemainingItem = new MenuItem("Adjust Today's Remaining...");
            MenuItem resetUsageItem = new MenuItem("Reset Today's Usage");
            MenuItem deleteItem = new MenuItem("Delete Category");
            ContextMenu contextMenu = new ContextMenu(setLimitItem, adjustRemainingItem, resetUsageItem, deleteItem);

            ListCell<Category> cell = new ListCell<>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        resetUsageItem.setDisable(true);
                    } else {
                        setText(formatCategoryDisplay(item));
                        resetUsageItem.setDisable(item.getDailyLimitMinutes() == null);
                    }
                }
            };

            setLimitItem.setOnAction(event -> {
                Category item = cell.getItem();
                if (item != null) {
                    categoryListView.getSelectionModel().select(item);
                    promptSetCategoryLimit(item);
                }
            });

            adjustRemainingItem.setOnAction(event -> {
                Category item = cell.getItem();
                if (item != null) {
                    categoryListView.getSelectionModel().select(item);
                    promptAdjustRemainingTime(item);
                }
            });

            resetUsageItem.setOnAction(event -> {
                Category item = cell.getItem();
                if (item != null && item.getDailyLimitMinutes() != null) {
                    categoryListView.getSelectionModel().select(item);
                    promptResetCategoryUsage(item);
                }
            });

            deleteItem.setOnAction(event -> {
                Category item = cell.getItem();
                if (item != null) {
                    categoryListView.getSelectionModel().select(item);
                    promptDeleteCategory(item);
                }
            });

            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });
        categoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            updateSelectedCategoryLabel(newValue);
            updateControlAvailability();
        });

        timelineListView.setItems(timelineItems);
        timelineListView.setPlaceholder(new Label("No sessions recorded today"));
        timelineListView.setCellFactory(listView -> {
            MenuItem deleteItem = new MenuItem("Delete");
            ContextMenu contextMenu = new ContextMenu(deleteItem);

            ListCell<SessionViewModel> cell = new ListCell<>() {
                @Override
                protected void updateItem(SessionViewModel item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.asDisplayString());
                    }
                }
            };

            deleteItem.setOnAction(event -> {
                SessionViewModel item = cell.getItem();
                if (item != null) {
                    promptDeleteSession(item);
                }
            });

            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });

            return cell;
        });

        timelineListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                SessionViewModel selected = timelineListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    promptDeleteSession(selected);
                }
            }
        });

        startStopButton.setText("Start");
        startStopButton.setDisable(true);
        resetButton.setDisable(true);
        timerLabel.setText("00:00:00");
        selectedCategoryLabel.setText("Selected Category: -");
        remainingTimeLabel.setText("Remaining Today: -");
        startTimeLabel.setText("Start Time: -");
        statusLabel.setText("Status: Idle");

        loadCategories();
        refreshTimeline();
    }

    @FXML
    private void onAddCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Create a new category");
        dialog.setContentText("Name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                showError("Invalid category name", "Category name cannot be empty.");
            } else {
                LimitDialogResult limitResult = promptForDailyLimit(null);
                if (limitResult.cancelled()) {
                    return;
                }
                createCategory(trimmed, limitResult.limitMinutes());
            }
        });
    }

    @FXML
    private void onExportToday() {
        if (sessionService.getTodaySessions().isEmpty()) {
            showInfo("Nothing to export", "No sessions recorded today.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Today to iCalendar");
        fileChooser.setInitialFileName("TimeTracker-" + LocalDate.now() + ".ics");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("iCalendar Files", "*.ics"));
        File target = fileChooser.showSaveDialog(timerLabel.getScene().getWindow());
        if (target == null) {
            return;
        }
        try {
            String ics = sessionService.generateTodayIcs();
            Files.writeString(target.toPath(), ics, StandardCharsets.UTF_8);
            showInfo("Export complete", "Saved today's sessions to:\n" + target.getAbsolutePath());
        } catch (IOException e) {
            showError("Export failed", "Unable to write file: " + e.getMessage());
        }
    }

    @FXML
    private void onStartStop() {
        if (sessionService.isSessionRunning()) {
            handleStop();
        } else {
            handleStart();
        }
    }

    @FXML
    private void onReset() {
        stopTimer();
        sessionService.cancelActiveSession();
        timerLabel.setText("00:00:00");
        startTimeLabel.setText("Start Time: -");
        statusLabel.setText("Status: Idle");
        startStopButton.setText("Start");
        resetButton.setDisable(true);
        updateControlAvailability();
        updateSelectedCategoryLabel(categoryListView.getSelectionModel().getSelectedItem());
    }

    private void handleStart() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No category selected", "Please select a category before starting the timer.");
            return;
        }
        try {
            OptionalLong remaining = sessionService.getRemainingSecondsForCategoryToday(selected);
            Long allowedSeconds = null;
            if (remaining.isPresent()) {
                long remainingSeconds = remaining.getAsLong();
                if (remainingSeconds <= 0) {
                    showError("Daily limit reached", "No remaining time left today for " + selected.getName() + ".");
                    return;
                }
                allowedSeconds = remainingSeconds;
            }
            sessionService.startSession(selected, allowedSeconds);
            updateSelectedCategoryLabel(selected);
            sessionService.getActiveSession()
                    .ifPresent(active -> startTimeLabel.setText("Start Time: " + TimeUtils.formatHHmm(active.startTime())));
            statusLabel.setText("Status: Running");
            startStopButton.setText("Stop");
            resetButton.setDisable(false);
            startTimer();
            updateControlAvailability();
        } catch (IllegalStateException | IllegalArgumentException e) {
            showError("Cannot start session", e.getMessage());
        }
    }

    private void handleStop() {
        stopTimer();
        Optional<Session> saved = sessionService.stopSession();
        timerLabel.setText("00:00:00");
        startTimeLabel.setText("Start Time: -");
        statusLabel.setText("Status: Idle");
        startStopButton.setText("Start");
        resetButton.setDisable(true);
        updateControlAvailability();
        refreshTimeline();
        updateSelectedCategoryLabel(categoryListView.getSelectionModel().getSelectedItem());
    }

    private void loadCategories() {
        Category previouslySelected = categoryListView.getSelectionModel().getSelectedItem();
        Integer selectedId = previouslySelected != null ? previouslySelected.getId() : null;
        categoryItems.setAll(categoryService.getAllCategories());
        if (!categoryItems.isEmpty()) {
            if (selectedId != null) {
                selectCategoryById(selectedId);
            } else if (categoryListView.getSelectionModel().getSelectedItem() == null) {
                categoryListView.getSelectionModel().selectFirst();
            }
        }
        updateControlAvailability();
        updateSelectedCategoryLabel(categoryListView.getSelectionModel().getSelectedItem());
    }

    private void refreshTimeline() {
        timelineItems.setAll(sessionService.getTodaySessions());
        updateSelectedCategoryLabel(categoryListView.getSelectionModel().getSelectedItem());
        updateControlAvailability();
    }

    private boolean updateRemainingTimeLabel(Category category) {
        if (category == null) {
            remainingTimeLabel.setText("Remaining Today: -");
            return false;
        }
        OptionalLong remaining = sessionService.getRemainingSecondsForCategoryToday(category);
        if (remaining.isEmpty()) {
            remainingTimeLabel.setText("Remaining Today: Unlimited");
            return false;
        }
        long seconds = remaining.getAsLong();
        remainingTimeLabel.setText("Remaining Today: " + TimeUtils.formatHHmmss(seconds));
        return seconds <= 0;
    }

    private void selectCategoryById(int categoryId) {
        for (int i = 0; i < categoryItems.size(); i++) {
            if (categoryItems.get(i).getId() == categoryId) {
                categoryListView.getSelectionModel().select(i);
                return;
            }
        }
        categoryListView.getSelectionModel().clearSelection();
    }

    private void createCategory(String name, Integer dailyLimitMinutes) {
        try {
            Category category = categoryService.createCategory(name, dailyLimitMinutes);
            categoryItems.add(category);
            selectCategoryById(category.getId());
            updateSelectedCategoryLabel(categoryListView.getSelectionModel().getSelectedItem());
            updateControlAvailability();
        } catch (IllegalArgumentException | IllegalStateException e) {
            showError("Unable to create category", e.getMessage());
        }
    }

    private String formatCategoryDisplay(Category category) {
        if (category.getDailyLimitMinutes() == null) {
            return category.getName();
        }
        return category.getName() + " (" + category.getDailyLimitMinutes() + " dk/day)";
    }

    private void promptSetCategoryLimit(Category category) {
        LimitDialogResult result = promptForDailyLimit(category.getDailyLimitMinutes());
        if (result.cancelled()) {
            return;
        }
        try {
            Category updated = categoryService.updateCategoryLimit(category.getId(), result.limitMinutes());
            replaceCategoryInList(updated);
            updateSelectedCategoryLabel(categoryListView.getSelectionModel().getSelectedItem());
            updateControlAvailability();
        } catch (IllegalArgumentException | IllegalStateException e) {
            showError("Unable to update category", e.getMessage());
        }
    }

    private void replaceCategoryInList(Category updatedCategory) {
        for (int i = 0; i < categoryItems.size(); i++) {
            if (categoryItems.get(i).getId() == updatedCategory.getId()) {
                categoryItems.set(i, updatedCategory);
                categoryListView.getSelectionModel().select(i);
                return;
            }
        }
        categoryItems.add(updatedCategory);
        categoryListView.getSelectionModel().select(updatedCategory);
    }

    private LimitDialogResult promptForDailyLimit(Integer existingLimit) {
        while (true) {
            TextInputDialog limitDialog = new TextInputDialog(existingLimit == null ? "" : existingLimit.toString());
            limitDialog.setTitle("Daily Limit");
            limitDialog.setHeaderText("Set a daily limit in minutes (leave blank for unlimited).");
            limitDialog.setContentText("Minutes:");
            Optional<String> response = limitDialog.showAndWait();
            if (response.isEmpty()) {
                return LimitDialogResult.cancelledResult();
            }
            String trimmed = response.get().trim();
            if (trimmed.isEmpty()) {
                return LimitDialogResult.unlimited();
            }
            try {
                int minutes = Integer.parseInt(trimmed);
                if (minutes <= 0) {
                    throw new NumberFormatException();
                }
                return LimitDialogResult.ofMinutes(minutes);
            } catch (NumberFormatException e) {
                showError("Invalid limit", "Please enter a positive number of minutes or leave blank for unlimited.");
            }
        }
    }

    private void handleSessionExpiredByLimit() {
        stopTimer();
        sessionService.stopSession();
        timerLabel.setText("00:00:00");
        startTimeLabel.setText("Start Time: -");
        statusLabel.setText("Status: Limit reached");
        startStopButton.setText("Start");
        resetButton.setDisable(true);
        refreshTimeline();
    }

    private record LimitDialogResult(boolean cancelled, Integer limitMinutes) {
        static LimitDialogResult cancelledResult() {
            return new LimitDialogResult(true, null);
        }

        static LimitDialogResult unlimited() {
            return new LimitDialogResult(false, null);
        }

        static LimitDialogResult ofMinutes(int minutes) {
            return new LimitDialogResult(false, minutes);
        }
    }

    private void updateSelectedCategoryLabel(Category category) {
        if (category == null) {
            selectedCategoryLabel.setText("Selected Category: -");
            remainingTimeLabel.setText("Remaining Today: -");
            if (!sessionService.isSessionRunning()) {
                statusLabel.setText("Status: Idle");
            }
        } else {
            StringBuilder builder = new StringBuilder("Selected Category: ").append(category.getName());
            if (category.getDailyLimitMinutes() != null) {
                builder.append(" (").append(category.getDailyLimitMinutes()).append(" dk/day limit)");
            }
            selectedCategoryLabel.setText(builder.toString());
            boolean limitReached = updateRemainingTimeLabel(category);
            if (!sessionService.isSessionRunning()) {
                statusLabel.setText(limitReached ? "Status: Limit reached" : "Status: Idle");
            }
        }
    }

    private void updateControlAvailability() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();
        boolean running = sessionService.isSessionRunning();
        boolean hasSelection = selected != null;
        boolean limitReached = false;
        if (hasSelection && !running) {
            OptionalLong remaining = sessionService.getRemainingSecondsForCategoryToday(selected);
            limitReached = remaining.isPresent() && remaining.getAsLong() <= 0;
        }
        startStopButton.setDisable(!running && (!hasSelection || limitReached));
        addCategoryButton.setDisable(running);
        categoryListView.setDisable(running);
        if (!running) {
            startStopButton.setText("Start");
        }
    }

    private void startTimer() {
        if (tickingTimeline != null) {
            tickingTimeline.stop();
        }
        timerLabel.setText(sessionService.getActiveSession()
                .map(active -> {
                    Long allowedSeconds = active.allowedSeconds();
                    if (allowedSeconds != null) {
                        return TimeUtils.formatHHmmss(allowedSeconds);
                    }
                    return "00:00:00";
                })
                .orElse("00:00:00"));
        tickingTimeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> updateTimerDisplay()));
        tickingTimeline.setCycleCount(Timeline.INDEFINITE);
        tickingTimeline.playFromStart();
    }

    private void stopTimer() {
        if (tickingTimeline != null) {
            tickingTimeline.stop();
            tickingTimeline = null;
        }
    }

    private void updateTimerDisplay() {
        sessionService.getActiveSession().ifPresent(active -> {
            long elapsedSeconds = Math.max(0, Duration.between(active.startTime(), LocalDateTime.now()).getSeconds());
            Long allowedSeconds = active.allowedSeconds();
            if (allowedSeconds != null) {
                long remaining = Math.max(0, allowedSeconds - elapsedSeconds);
                if (remaining <= 0) {
                    handleSessionExpiredByLimit();
                    return;
                }
                timerLabel.setText(TimeUtils.formatHHmmss(remaining));
            } else {
                timerLabel.setText(TimeUtils.formatHHmmss(elapsedSeconds));
            }
        });
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void promptResetCategoryUsage(Category category) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Reset Daily Usage");
        confirmation.setHeaderText("Reset today's usage for \"" + category.getName() + "\"?");
        confirmation.setContentText("This will restore the full daily limit for the rest of today. Existing entries remain in the timeline.");
        Optional<ButtonType> response = confirmation.showAndWait();
        if (response.isPresent() && response.get() == ButtonType.OK) {
            boolean wasRunning = sessionService.getActiveSession()
                    .map(active -> active.category().getId() == category.getId())
                    .orElse(false);
            if (wasRunning) {
                stopTimer();
                timerLabel.setText("00:00:00");
                startTimeLabel.setText("Start Time: -");
                statusLabel.setText("Status: Idle");
                startStopButton.setText("Start");
                resetButton.setDisable(true);
            }
            try {
                sessionService.resetUsageForToday(category);
                updateSelectedCategoryLabel(categoryListView.getSelectionModel().getSelectedItem());
                updateControlAvailability();
            } catch (IllegalStateException | IllegalArgumentException e) {
                showError("Unable to reset usage", e.getMessage());
            }
        }
    }

    private void promptAdjustRemainingTime(Category category) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Adjust Today's Remaining Time");
        dialog.setHeaderText("Set today's remaining time for \"" + category.getName() + "\".");
        dialog.setContentText("Minutes (leave blank for unlimited today):");
        Optional<String> response = dialog.showAndWait();
        if (response.isEmpty()) {
            return;
        }
        String trimmed = response.get().trim();
        try {
            if (trimmed.isEmpty()) {
                sessionService.setRemainingSecondsForToday(category, null);
            } else {
                long minutes = Long.parseLong(trimmed);
                if (minutes <= 0) {
                    throw new NumberFormatException();
                }
                sessionService.setRemainingSecondsForToday(category, minutes * 60L);
            }
            updateSelectedCategoryLabel(categoryListView.getSelectionModel().getSelectedItem());
            updateControlAvailability();
        } catch (NumberFormatException e) {
            showError("Invalid input", "Please enter a positive number of minutes or leave blank for unlimited.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            showError("Unable to adjust remaining time", e.getMessage());
        }
    }

    private void promptDeleteCategory(Category category) {
        if (sessionService.getActiveSession()
                .map(active -> active.category().getId() == category.getId())
                .orElse(false)) {
            showError("Cannot delete category", "Stop the running session before deleting this category.");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Category");
        confirmation.setHeaderText("Delete \"" + category.getName() + "\"?");
        confirmation.setContentText("All recorded sessions for this category will be removed.");
        Optional<ButtonType> response = confirmation.showAndWait();
        if (response.isPresent() && response.get() == ButtonType.OK) {
            try {
                sessionService.deleteSessionsForCategory(category.getId());
                categoryService.deleteCategory(category.getId());
                removeCategoryFromList(category.getId());
                refreshTimeline();
            } catch (IllegalStateException e) {
                showError("Unable to delete category", e.getMessage());
            }
        }
    }

    private void removeCategoryFromList(int categoryId) {
        int currentIndex = categoryListView.getSelectionModel().getSelectedIndex();
        for (int i = 0; i < categoryItems.size(); i++) {
            if (categoryItems.get(i).getId() == categoryId) {
                categoryItems.remove(i);
                if (currentIndex >= categoryItems.size()) {
                    currentIndex = categoryItems.size() - 1;
                }
                break;
            }
        }
        if (categoryItems.isEmpty()) {
            categoryListView.getSelectionModel().clearSelection();
        } else if (currentIndex >= 0) {
            categoryListView.getSelectionModel().select(currentIndex);
        }
        updateSelectedCategoryLabel(categoryListView.getSelectionModel().getSelectedItem());
        updateControlAvailability();
    }

    private void promptDeleteSession(SessionViewModel session) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Session");
        confirmation.setHeaderText("Delete this session?");
        confirmation.setContentText(session.asDisplayString());
        Optional<ButtonType> response = confirmation.showAndWait();
        if (response.isPresent() && response.get() == ButtonType.OK) {
            try {
                boolean deleted = sessionService.deleteSession(session.id());
                if (deleted) {
                    refreshTimeline();
                    timelineListView.getSelectionModel().clearSelection();
                    updateSelectedCategoryLabel(categoryListView.getSelectionModel().getSelectedItem());
                } else {
                    showError("Unable to delete session", "The session could not be found. It may have already been deleted.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                showError("Unable to delete session", e.getMessage());
            }
        }
    }
}
