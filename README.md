# TimeTracker+

TimeTracker+ is a desktop companion built with JavaFX and SQLite that helps you plan, track, and review focused work sessions by category.

## Overview

- Track time against flexible categories with optional daily limits.
- Keep an at-a-glance timeline of today’s sessions and manage them in bulk.
- Export the day’s work as an iCalendar (`.ics`) file for calendar sync or reporting.
- Run locally with a lightweight SQLite database—no services to configure.

## Features

- **Category management**: Create, rename, delete, and enforce daily minute caps per category.
- **Session tracking**: Start, stop, cancel, and edit sessions with automatic limit enforcement.
- **Timeline view**: See today’s history in a dedicated list, delete entries, or reset usage in one click.
- **Usage adjustments**: Reset a category’s usage or override remaining minutes for the current day.
- **Historical reporting**: Filter any custom date range, browse past sessions, and review per-category summaries.
- **Range exports**: Generate standards-compliant `.ics` calendars or `.csv` reports for any date window in a couple of clicks.

## Architecture

```text
JavaFX View (main-view.fxml)
        │
        ▼
MainController (UI behaviour)
        │
        ├─ CategoryService ── CategoryDao ─┐
        │                                  │
        └─ SessionService  ── SessionDao ──┼─ DatabaseManager → SQLite (timetracker.db)
                                           │
                    DatabaseInitializer ───┘
```

- **View**: The JavaFX scene is defined in `src/main/resources/com/timetracker/view/main-view.fxml`.
- **Controller**: `MainController` wires UI events to services and handles user interactions.
- **Services**: `CategoryService` and `SessionService` implement business rules (limit handling, active sessions, ICS generation).
- **Persistence**: DAOs wrap SQL access and share `DatabaseManager` for connections. `DatabaseInitializer` runs on app startup to create or upgrade tables.
- **Models & utilities**: Plain model objects (`Category`, `Session`, `SessionViewModel`, `SessionDto`) and `TimeUtils` keep formatting, conversions, and presentation logic out of the UI.

## Project Layout

```text
TimeTracker/
├── run.sh                   # Convenience script to launch with Maven Wrapper
├── pom.xml                  # Maven build configuration
├── docs/
│   └── ARCHITECTURE.md      # In-depth architecture and layering guide
├── src/
│   ├── main/java/com/timetracker/
│   │   ├── controller/      # JavaFX controllers & custom cells
│   │   ├── service/         # Business logic and orchestration
│   │   ├── dao/             # Data access layer
│   │   ├── db/              # Connection + schema bootstrap
│   │   ├── model/           # Domain + view models
│   │   └── util/            # Shared helpers
│   ├── main/resources/com/timetracker/view/
│   │   └── main-view.fxml   # JavaFX layout
│   └── test/java/com/timetracker/service/
│       └── SessionServiceTest.java  # Service-level unit tests
└── target/                  # Maven build output

> Generated artifacts such as `target/` and `timetracker.db` are ignored by Git.
```

## Getting Started

### Prerequisites

- **Java Development Kit (JDK) 21** or newer. The bundled Maven Wrapper downloads the right Maven version automatically.

### Quick start

1. From the project root, execute the run script:

   ```bash
   ./run.sh
   ```

   The script verifies your Java version and launches the app via `./mvnw -q -DskipTests javafx:run`.

2. On Windows, use PowerShell or Command Prompt and run:

   ```powershell
   .\mvnw.cmd -q -DskipTests javafx:run
   ```

### Building the project

Compile and package the application (without running the UI) with:

```bash
./mvnw clean install
```

Artifacts and compiled classes are placed in `target/`.

### Launching from an IDE

- Import the project as a Maven project.
- Ensure your SDK is set to JDK 21.
- Run the `TimeTrackerApp` class; Maven/IDE will manage the JavaFX runtime dependencies.

## Data & persistence

- The SQLite database (`timetracker.db`) lives in the project root by default.
- `DatabaseInitializer` applies schema changes automatically, so you can upgrade without manual migrations.
- Categories, sessions, and per-day usage overrides are stored in separate tables; deleting a category cascades to its sessions and usage records.

## Reporting & exports

- Use the Today tab to export the current day as either `.ics` or `.csv`.
- Switch to the History tab to pick any start/end date, review sessions, and export the same window to ICS or CSV.
- The category summary table highlights total minutes per category so you can spot trends at a glance.

## Troubleshooting

- If the UI fails to load, double-check that JavaFX modules are available—`pom.xml` already declares the required dependencies.
- Delete `timetracker.db` to start with a fresh database (all data will be removed).

## Development

- Run the unit tests (if present) with `./mvnw test`.
- Use `./mvnw javafx:run` to launch directly from Maven without the helper script.
- When contributing, keep runtime artifacts (`target/`, `*.log`, `*.db`) out of version control—see `.gitignore` for the current list.
- Service-level tests live in `src/test/java`; Mockito-powered fakes keep SessionService logic testable without SQLite.

## Contributing

We welcome contributions! Review the guidelines in [`CONTRIBUTING.md`](CONTRIBUTING.md) for branching strategy, coding standards, and pull-request expectations.

## License

This project currently has no explicit license. If you plan to use or distribute TimeTracker+, please contact the maintainers to clarify terms or open an issue to propose a license addition.
