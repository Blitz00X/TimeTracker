# UI Specification

## Layout (main-view.fxml)
- **Left panel: Categories**
  - ListView of categories with colored dots and context menu (set daily limit, adjust today’s remaining, reset today, delete).
  - Buttons: `Add Category`, `Edit`, `Delete Category` plus helper label.
- **Center panel: Timer & Today**
  - Timer block shows HH:MM:SS, selected category, remaining today, start time, and status.
  - Buttons: `Start/Stop`, `Reset` (cancel active), mode ComboBox (placeholder values: Normal/Pomodoro/Locked Mode).
  - Timeline: ListView of today’s sessions with color indicator and `Edit…`/`Delete` via context menu or Delete key.
  - Export buttons: `Export Day (.ics)` and `Export Day (.csv)` enabled when there is data.
  - Today Summary placeholder ListView (reserved for future summary rows).
- **Right panel: TabPane**
  - **Today tab**: Info stub pointing to center panel.
  - **Auto Usage tab**: date picker, Pause checkbox, Refresh, Export (.csv) buttons; TableView with columns App/Site, Domain/Title (URL), Duration (HH:mm:ss); disabled when empty.
  - **History tab**: date range pickers (From/To), Refresh; error label for invalid ranges; ListView of sessions in range; TableView of category summary (Category, Minutes); export buttons for ICS/CSV enabled when data exists.

## Interactions
- Selecting a category enables Start when limits permit; controls disable while a session is running.
- Start: begins timer against selected category; Stop saves the session; Reset cancels without saving.
- Category context actions call dialogs for limits and confirmations for deletions/resets.
- Session context menu permits edit/delete; Delete key removes the selected session in timeline/history.
- Date pickers default to last 7 days; validation prevents end < start.
- Auto usage Refresh triggers aggregation for the selected date; Pause toggles capture in `ActivityTrackingService`.
- Export actions prompt for file destination via OS file chooser.

## Compact Window
- Always-on-top utility window showing status, category ComboBox (mirrors main list), Start/Stop button, timer label, and “Open Full View” button.
- Appears when the main window is minimized; hides when restored; start/stop actions call back into `MainController` so both windows stay in sync.

## Styling
- Light theme via inline styles in FXML (white panels on gray background, subtle borders, bold section labels). Category colors derive from `CategoryColorUtil` for consistent dots across lists.
