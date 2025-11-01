# Contributing to TimeTracker+

Thanks for your interest in improving TimeTracker+! This guide outlines how to get a local development environment running, the standards we follow, and how to propose changes.

## Getting started

1. **Install prerequisites**
   - JDK 21 (or newer)
   - Git
   - Optional: an IDE with JavaFX support (IntelliJ IDEA or VS Code)

2. **Clone the repository**
   ```bash
   git clone https://github.com/<your-account>/TimeTracker.git
   cd TimeTracker
   ```

3. **Run the application**
   ```bash
   ./run.sh
   ```
   The script ensures your Java version is compatible and launches the JavaFX application using the Maven Wrapper.

## Development workflow

1. Create a feature branch from `main` describing the change you plan to make.
2. Make your changes and keep commits focused; small, logical commits make reviews faster.
3. Run the test suite (if present) with:
   ```bash
   ./mvnw test
   ```
4. Format and lint your code according to the style used in the project (separate imports, meaningful names, and concise methods). Java code should follow standard conventions recommended by Oracle.
5. Update documentation (README, Javadoc, or in-code comments) when you add or change functionality.
6. Open a pull request with a clear title and description. Reference related issues when relevant and summarise your changes, testing, and any follow-up work.

## Pull request checklist

- [ ] Feature or bug fix is scoped to a single PR.
- [ ] Tests cover new behaviour or regressions are prevented another way.
- [ ] Build passes locally via `./mvnw clean verify` (or `./mvnw test` if verify is too heavy).
- [ ] Documentation is updated where needed (README, inline comments, changelog entries).
- [ ] `target/`, database files, IDE configs, and other generated artifacts are not committed.

## Reporting issues

When you file an issue, please include:

- A clear, descriptive title.
- Steps to reproduce, expected behaviour, and actual behaviour.
- Logs, stack traces, or screenshots if they help explain the problem.
- Your OS, Java version, and any additional environment details.

## Code of conduct

Be respectful, inclusive, and collaborative in all project spaces. Harassment or exclusionary behaviour is not tolerated. If you encounter any issues, contact the maintainers privately so we can address them quickly.

Thanks for helping make TimeTracker+ better!
