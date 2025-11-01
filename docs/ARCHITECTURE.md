# Architecture Overview

This document provides a deeper look at TimeTracker+'s architecture beyond the summary in the main README.

## High-level structure

```
┌───────────────────────────────────────────────────────────────────┐
│                           TimeTracker+                            │
├───────────────────────────────────────────────────────────────────┤
│                           Presentation                            │
│ ┌───────────────┐   ┌───────────────────────────┐                 │
│ │ JavaFX FXML   │ → │ MainController            │ ──┐              │
│ │ (`main-view`) │   │ (UI state & events)       │   │              │
│ └───────────────┘   └───────────────────────────┘   │              │
│                                                     ▼              │
│                           Application                              │
│ ┌───────────────────┐   ┌────────────────────┐   ┌────────────────┐│
│ │ CategoryService   │   │ SessionService     │   │ TimeUtils       ││
│ │ (categories CRUD) │   │ (sessions, limits) │   │ (format helpers)││
│ └───────────┬───────┘   └──────┬────────────┘   └────────┬───────┘│
│             │                  │                         │        │
│             ▼                  ▼                         │        │
│                      Persistence Layer                             │
│ ┌────────────────────┐  ┌────────────────────┐   ┌────────────────┐│
│ │ CategoryDao        │  │ SessionDao         │   │ DatabaseManager ││
│ │ (categories table) │  │ (sessions, resets) │   │ (connections)   ││
│ └────────────┬───────┘  └─────────┬──────────┘   └────────┬───────┘│
│              │                    │                         │        │
│              ▼                    ▼                         ▼        │
│                         SQLite (`timetracker.db`)                    │
└──────────────────────────────────────────────────────────────────────┘
```

## Layer responsibilities

### Presentation

- **FXML (`src/main/resources/com/timetracker/view/main-view.fxml`)**  
  Declares the UI structure and is loaded via `FXMLLoader` in `TimeTrackerApp`.

- **`MainController`**  
  Mediates between user actions and the services. It updates UI state, handles context menus, keyboard shortcuts, and orchestrates session lifecycles.

- **History tab components**  
  Date pickers, the historical session list, and the category summary table project reporting data and trigger export actions via the controller.

### Application services

- **`CategoryService`**  
  Coordinates category CRUD operations and limit management. It ensures the UI stores and retrieves categories via the DAO layer.

- **`SessionService`**  
  Owns active session state, enforces daily limits (stop/cancel logic), produces view models for the timeline, powers historical queries, generates `.ics`/`.csv` exports, and supports session editing.

- **`TimeUtils`**  
  Centralises date/time formatting and conversions to keep UI code lean.

- **View models**  
  `SessionViewModel` and `CategorySummaryViewModel` keep presentation strings and summary rows out of DAOs and services.

### Persistence

- **`CategoryDao` & `SessionDao`**  
  Implement SQL operations using `java.sql`. `SessionDao` also tracks per-day overrides via the `category_usage_resets` table.

- **`DatabaseManager`**  
  Provides connections to the SQLite database and configures the connection URL. Managed as a singleton for simplicity.

- **`DatabaseInitializer`**  
  Executes on app startup (via `TimeTrackerApp#init`) to create tables if they do not exist and to perform additive migrations (adding columns when missing).

## Data model

| Table                     | Purpose                                  | Key Columns                                    |
|---------------------------|-------------------------------------------|------------------------------------------------|
| `categories`              | Stores user-defined categories            | `id`, `name`, `daily_limit_minutes`            |
| `sessions`                | Records each tracking session             | `id`, `category_id`, `start_time`, `end_time`  |
| `category_usage_resets`   | Persists per-day usage overrides/resets   | `category_id`, `usage_date`, `offset_seconds`, `override_limit_seconds` |

- Foreign keys ensure that deleting a category cascades to associated sessions and usage resets.
- Time is stored as ISO-8601 strings; conversions occur in the DAO layer.

## Error handling and validation

- Services guard against illegal arguments (e.g., null categories, negative durations).
- SQL exceptions during initialisation are wrapped in an `IllegalStateException` to fail fast.
- UI prompts use dialogs to confirm destructive actions and to collect user input safely.

## Extensibility ideas

- Add persistence of user preferences (window size, default export location) by introducing a configuration DAO.
- Implement auditing or analytics by adding another table or exporting aggregated reports.
- Introduce automated tests for services using an in-memory SQLite database (achievable via `jdbc:sqlite::memory:`).

Feel free to expand this document as the application evolves.
