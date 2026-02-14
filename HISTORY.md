# Project History

## Origins

Emulator8086 was originally developed as a personal hobby project. The exact date of its initial creation is unknown, but it was first published to GitHub in **November 2015**, with one subsequent commit in **April 2016**.

## 2022 Revival

In **April 2022**, the project was revisited with the goal of modernizing the build system and making the application functional again.

Key activities during this phase:
- Reviewed legacy files (`ali`, `asd.asm`, etc.) — original assembly source files whose purpose had been long forgotten.
- Removed IDE-specific files and committed build artifacts (e.g., a `bin` folder) from the repository.
- Added a `.gitignore` file to prevent future accidental commits of build artifacts.
- Migrated the project to a **Gradle** build system, including configuring the main class.
- Fixed character encoding issues — source files were not UTF-8 encoded, causing compilation errors with unexpected characters.
- Removed unused imports to resolve remaining compilation issues.
- Performed a full repository history rewrite to clean up the commit history.

### Application Impressions (2022)

Once running, the application featured a functional Swing-based GUI with:
- A menu bar for opening and running assembly files.
- Options for changing the representation of memory and addresses (binary, hexadecimal, decimal).
- A built-in help section (whose linked website was updated to a working URL).

Notable limitation: there was no stop button, so an assembly program resulting in an infinite loop would run indefinitely.

## February 2026 — Modernization

In **February 2026**, a comprehensive modernization effort was undertaken with the assistance of Claude (AI). This update focused on code quality, maintainability, and test coverage.

Key changes:
- **Build system upgrade**: Updated Gradle wrapper and migrated `build.gradle` to Kotlin DSL (`build.gradle.kts`).
- **Code refactoring**: Significant refactoring across core components — `Assembler`, `Processor`, `RegisterConstants`, `RegisterType`, and GUI panels — reducing the codebase by over 600 lines while preserving functionality.
- **Improved code structure**: Introduced enums, simplified control flow, and applied modern Java conventions throughout.
- **Test suite**: Added unit tests for the `Assembler`, `Processor`, and memory subsystems, establishing a foundation for regression testing.
- **GUI improvements**: Refactored menu and display panels for better readability and maintainability.
