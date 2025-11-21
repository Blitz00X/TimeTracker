# TimeTracker+ Software Requirements Specification

## 1. Purpose and Scope
- Describe requirements for the current TimeTracker+ desktop app built with JavaFX and SQLite.
- Covers manual session tracking, category limits, reporting/export, and lightweight automatic activity capture on Linux.
- Data stays local; no network services are required.

## 2. Users and Stakeholders
- **Individual contributor**: tracks focus blocks against categories and daily limits.
- **Team member/lead**: reviews exports (CSV/ICS) for reporting.
- **Developer**: maintains code, adds features, and debugs tracking.

## 3. System Overview
- Manual timer with category-based sessions; optional daily minute caps plus per-day overrides.
- Timeline of today’s sessions and a history browser for arbitrary date ranges with category summaries.
- Export sessions to ICS/CSV (day or date range).
- Background activity tracker (Linux) that samples active window and optional URL, aggregates to per-app/site totals, and can be paused.
- All data is persisted in a local SQLite database (`timetracker.db`) created at startup.

## 4. Functional Requirements
- **FR-1 Category management**: create categories, configure/change optional daily limits, delete categories; list reflects current limits.
- **FR-2 Daily limit controls**: per-day override of remaining time; reset today’s usage to restore the limit; disable reset when no limit exists.
- **FR-3 Manual session lifecycle**: start/stop a session for the selected category; enforce remaining time if limited; cancel/reset an active timer without saving.
- **FR-4 Session editing**: edit start/end timestamps of existing sessions; delete sessions individually via context menu or Delete key.
- **FR-5 Timeline (Today)**: list today’s sessions chronologically with category color coding and durations.
- **FR-6 History**: select start/end dates, list sessions in range, and show per-category total minutes for that window.
- **FR-7 Export**: export today or a custom range to ICS and CSV with formatted timestamps; allow user-selected save location.
- **FR-8 Compact window**: optional always-on-top mini window that mirrors start/stop controls and current timer state when the main window is minimized.
- **FR-9 Auto activity capture**: background polling of active app/window title; optional browser URL capture (Chromium debug port or window-title heuristic); record events and idle state changes.
- **FR-10 Auto aggregation/reporting**: roll captured events into sessions and daily totals (app/domain/url, seconds) on a 5-minute schedule or on-demand refresh; export daily auto totals to CSV; allow pausing capture.
- **FR-11 Configuration**: runtime settings via environment variables (`TT_POLL_SECONDS`, `TT_IDLE_MINUTES`, `TT_CAPTURE_URLS`, `TT_REDACT_QUERY`); defaults apply when unset.
- **FR-12 Data handling**: initialize/upgrade schema automatically; cascade deletes from categories to dependent sessions and resets; keep data local.
- **FR-13 Error handling/validation**: block invalid inputs (empty names, non-positive limits, end before start); surface user-facing alerts on failure; ensure timer buttons disable appropriately.

## 5. Data Requirements
- Persist categories, sessions, per-day usage offsets/overrides, captured activity events, aggregated activity sessions, and daily totals in SQLite.
- Store timestamps as ISO-8601 strings; enforce foreign keys between categories and sessions/usage resets.

## 6. Non-Functional Requirements
- **Platform**: Java 21 runtime; JavaFX UI; automatic activity capture currently implemented for Linux/X11. Other OSes fall back to no-op capture but manual tracking still works.
- **Performance**: UI must remain responsive while background jobs run; polling default 10s; aggregation default every 5 minutes.
- **Reliability**: Database initialization and additive migrations must be idempotent. Failures in auto tracking must not crash the UI.
- **Usability**: Provide straightforward dialogs for category/time inputs and confirmations for destructive actions.
- **Security/Privacy**: All data is local; URL capture is opt-in and query strings can be redacted; no external transmission.
- **Portability**: Use standard Java and SQLite; no external services beyond optional browser debug endpoint for URLs.

## 7. Assumptions and Constraints
- Users grant the app permission to capture global input on Linux for idle detection (via JNativeHook); if unavailable, idle events are skipped.
- Browser URL capture requires a Chromium-based browser started with `--remote-debugging-port` (default 9222); otherwise domain/title heuristics apply.
- Limits and usage calculations assume the system clock is accurate; sessions cannot overlap because only one manual session may run at a time.
- Database file resides in the working directory; deleting it resets all data.
