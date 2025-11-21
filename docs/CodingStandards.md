# Coding Standards

## General
- Target Java 21; prefer immutable data structures and `record` where appropriate.
- Keep UI controllers thin—delegate validation and business rules to services; keep SQL in DAOs.
- Use `Objects.requireNonNull` / argument validation with clear `IllegalArgumentException` or `IllegalStateException` messages.
- Favor explicit formatting helpers (e.g., `TimeUtils`) to keep presentation logic centralized.

## Naming & Structure
- Classes and enums: PascalCase; methods/fields: camelCase; constants: UPPER_SNAKE.
- Packages follow role-based grouping (`controller`, `service`, `dao`, `db`, `tracking`, `model`, `util`).
- Keep one public class per file; use package-private classes for UI cells and helpers when scope is limited.

## Persistence
- Always use prepared statements; close resources with try-with-resources.
- Keep SQL readable using text blocks; align column lists; add indexes when queries require ordering/filtering.
- Schema changes must be additive and handled in `DatabaseInitializer` with safe duplicate-column handling.

## Concurrency & State
- Guard shared mutable state (e.g., `SessionService` active session) with synchronization or thread-safe structures.
- Background executors should be daemon threads and shut down in `Application#stop`.
- UI updates occur on the JavaFX thread; avoid blocking calls in event handlers.

## Logging & Errors
- Use SLF4J (`LoggerFactory`) for diagnostics in services/tracking; avoid `System.out` except for startup notices.
- Fail fast on unrecoverable DB issues; for optional features (auto tracking, idle detection) log and degrade gracefully.

## Testing
- Mock DAOs for service tests (Mockito + JUnit 5). Capture arguments to validate persistence values.
- Prefer deterministic time inputs in tests; avoid sleeping when verifying durations.
- Add manual test notes in `TestPlan.md` when automation is impractical (UI/FX interactions).

## UI Guidelines
- Define layout in FXML; keep inline styles minimal and consistent with existing light theme.
- Use view models to carry formatted strings; avoid embedding formatting in the FXML/controller logic multiple times.
- Provide context menus for list interactions; respect keyboard shortcuts (e.g., Delete to remove items) where already used.
