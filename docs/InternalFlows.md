# Internal Flows

## Manual Timer
- **Start**: MainController validates selection and remaining time → `SessionService.startSession(category, allowedSeconds)` creates in-memory ActiveSession → UI timer starts → manual start event stored in `activity_events`.
- **Tick**: Timeline updates every second; when a limit is set, remaining time counts down and auto-stops when exhausted.
- **Stop**: `SessionService.stopSession()` clamps end time to allowedSeconds, converts to `Session`, and persists via `SessionDao`; UI refreshes timeline/history and emits manual stop event.
- **Reset/Cancel**: Clears ActiveSession without saving; resets timer labels; disables reset button.

## Category Limits & Adjustments
- **Set/Edit limit**: Context menu → `CategoryService.updateCategoryLimit` → DAO updates `daily_limit_minutes`; UI refreshes remaining label and button states.
- **Reset today**: Stores `offset_seconds` in `category_usage_resets` equal to already-used seconds, effectively restoring the full limit for the rest of the day.
- **Adjust remaining today**: Writes `override_limit_seconds` for the day (null/negative means unlimited today). Active sessions for that category must be stopped first.

## Session Management
- **Edit session**: Dialog collects new start/end times → `SessionService.updateSession` validates ordering, recomputes duration, and updates `sessions` row.
- **Delete session**: Controller calls `SessionService.deleteSession`; DAO removes by id; lists refresh. Delete key on list views triggers the same path.
- **Delete category**: Guard against running session; `SessionService.deleteSessionsForCategory` clears sessions/usage resets, then `CategoryService.deleteCategory` removes the category (cascades in DB ensure cleanup).

## History & Exports
- **History refresh**: Validate date range → load sessions with `SessionDao.findSessionsForDateRange` → compute per-category totals → enable/disable export buttons accordingly.
- **ICS export**: `SessionService.generateIcsForDateRange` builds VCALENDAR text with UTC DTSTAMP and local start/end; saved via `FileChooser`.
- **CSV export**: `SessionService.generateCsvForDateRange` outputs categories, start/end (yyyy-MM-dd HH:mm), and duration minutes.

## Auto Tracking
- **Event capture**: Background collector emits `FOCUS` events with app id/title/url; idle detector adds `IDLE_ON/OFF`. Manual start/stop also push events for alignment.
- **Aggregation**: `ActivityAggregationJob.aggregate(from, to, persist=true)` pulls events, builds contiguous sessions (split on focus/url/idles/locks), summarizes totals (app/domain/url, seconds), and upserts into `activity_sessions` & `activity_daily_totals`.
- **Reporting & export**: `MainController#refreshAutoUsage` triggers aggregate for the selected day, fetches totals via `ActivityReportingService`, and fills the Auto Usage table. Export builds a CSV of `appOrSite,url,duration` with formatted HH:mm:ss.

## Startup/Shutdown
- **Startup**: `DatabaseInitializer.initialize()` creates/migrates tables → activity tracking + idle detection start → aggregation scheduler begins.
- **Shutdown**: Executors are shut down; a final aggregation runs for today; tracking/idle detectors are closed.
