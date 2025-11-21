# Roadmap

## Near Term
- Add automated tests for CategoryService, auto-tracking aggregation, and CSV/ICS export edge cases.
- Surface tracking configuration in the UI (poll interval, idle threshold, URL capture toggle) instead of environment-only.
- Improve Today Summary block with per-category totals for the current day.

## Mid Term
- Extend auto tracking beyond Linux: Windows/macOS collectors and idle detection fallbacks; feature-gate by platform.
- Persist user preferences (window position, last selected category, export directory) and a settings dialog.
- Package distribution bundles (native image or platform-specific installers) to simplify installs.
- Add manual/auto timeline visualizations (stacked bars) for daily review.

## Long Term
- Import/export database snapshots and cleanup/retention tools.
- Pluggable data sync/export (e.g., WebDAV/Nextcloud) while keeping local-first defaults.
- Multi-profile or workspace support for separating personal/work categories and data.
- Accessibility/keyboard-first improvements and localization.
