# Database Schema & DDL

TimeTracker+ uses a single SQLite database (`timetracker.db`) created in the project root. Timestamps are stored as ISO-8601 strings.

## Tables
```sql
CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    daily_limit_minutes INTEGER
);

CREATE TABLE IF NOT EXISTS sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_id INTEGER NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    duration_minutes INTEGER NOT NULL,
    FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS category_usage_resets (
    category_id INTEGER NOT NULL,
    usage_date TEXT NOT NULL,
    offset_seconds INTEGER NOT NULL,
    override_limit_seconds INTEGER,
    PRIMARY KEY (category_id, usage_date),
    FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS activity_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ts TEXT NOT NULL,
    event_type TEXT NOT NULL,
    app_id TEXT,
    window_title TEXT,
    url TEXT,
    payload_json TEXT
);
CREATE INDEX IF NOT EXISTS idx_activity_events_ts ON activity_events(ts);

CREATE TABLE IF NOT EXISTS activity_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    start_ts TEXT NOT NULL,
    end_ts TEXT NOT NULL,
    app_id TEXT,
    window_title TEXT,
    url TEXT,
    source TEXT NOT NULL,
    is_idle INTEGER NOT NULL DEFAULT 0,
    UNIQUE (start_ts, app_id, url, source, is_idle)
);
CREATE INDEX IF NOT EXISTS idx_activity_sessions_start ON activity_sessions(start_ts);

CREATE TABLE IF NOT EXISTS activity_daily_totals (
    usage_date TEXT NOT NULL,
    app_id TEXT NOT NULL,
    domain TEXT,
    url TEXT,
    total_seconds INTEGER NOT NULL,
    source TEXT NOT NULL,
    PRIMARY KEY (usage_date, app_id, domain, url, source)
);
```

## Notes
- `daily_limit_minutes` may be NULL (unlimited). The UI formats limits in minutes.
- `category_usage_resets` stores per-day adjustments: `offset_seconds` subtracts prior usage; `override_limit_seconds` sets a per-day cap (NULL → use category limit, negative → unlimited today).
- `sessions` durations are persisted in minutes; exports compute human-readable strings.
- Auto-tracking uses three tables: raw `activity_events`, aggregated `activity_sessions`, and summarized `activity_daily_totals` (per date/app/domain/url/source).
- Additive migrations: `DatabaseInitializer` will attempt to add `daily_limit_minutes` and `override_limit_seconds` columns if they are missing; duplicate-column errors are tolerated.
