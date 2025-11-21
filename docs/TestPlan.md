# Test Plan

## Scope
Covers manual time tracking, category limits/adjustments, exports, auto activity aggregation, and UI interactions. Primary platform target is desktop (JavaFX) with Linux-specific auto capture.

## Test Levels
- **Unit**: Service logic with mocked DAOs (example: `SessionServiceTest`). Add cases for new business rules and tracking aggregation.
- **Integration**: Optional ad-hoc runs against a temporary SQLite DB to validate DAO queries and schema migrations.
- **Manual UI**: End-to-end checks using the packaged UI (JavaFX) for critical flows and exports.

## Environments
- Java 21; run via `./run.sh` or `./mvnw javafx:run`.
- For auto tracking: Linux with `xprop` installed; Chromium-based browser optional for URL capture; global input permissions for JNativeHook.

## Test Cases (Manual)
1) **Category CRUD & limits**
   - Create category with/without limit; edit limit; delete category; verify list refresh and cascaded session removal.
2) **Daily limit adjustments**
   - Set limit, start/stop multiple sessions; check remaining time updates. Reset today → remaining resets to full. Adjust remaining to explicit minutes/unlimited; verify Start availability.
3) **Start/Stop/Reset session**
   - Start timer; ensure Start disables and Reset enables. Stop saves entry to today’s timeline/history; Reset discards.
4) **Session edit/delete**
   - Edit start/end to extend/shorten; validate errors on end before start. Delete via context menu and Delete key.
5) **History range**
   - Set valid range with data → list populates and summary totals match durations. Invalid range (end<start) shows error and disables exports.
6) **Exports**
   - Export today and custom range to ICS/CSV; open files to ensure contents (UIDs, timestamps, durations) match sessions and categories.
7) **Auto usage capture (Linux)**
   - With `TT_CAPTURE_URLS=true`, start browser and switch apps; wait for polling; Refresh auto tab → entries show app/site and durations >0. Pause toggle stops growth; export CSV contains rows.
8) **Idle detection (Linux)**
   - With tracking running, stop using input past idle threshold; verify auto durations stop increasing; resume input → tracking resumes.
9) **Compact window**
   - Minimize main window → compact window appears; start/stop from compact; restore main window; states stay in sync.

## Regression & Edge cases
- Zero/negative limit inputs rejected; empty category name rejected.
- Overlapping manual sessions prevented by design (only one active allowed).
- Handling when auto tracking is unavailable (non-Linux or missing `xprop`): app continues, auto table stays empty without errors.
- Database upgrade: launching on existing DB without optional columns should succeed (no crashes on duplicate column errors).

## Automation Notes
- Prefer service-level tests with Mockito for new business logic (limits, adjustments, exports). For aggregator logic, feed synthetic `ActivityEvent` lists into `ActivityAggregator` and assert sessions/totals.
- Use temporary SQLite files for DAO smoke tests if needed; clean up files after runs.
