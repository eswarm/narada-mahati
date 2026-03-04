# Agents

This project uses specialized sub-agents to handle certain tasks.

## Project Context (Copilot Instructions)
- Product: Narada is a multi-platform app with Android and desktop targets and shared Kotlin code.
- Scope: The repo includes app modules (`narada`, `mahati`) and a shared Kotlin module for cross-platform logic.
- Responsibilities: Focus on app logic, UI, and shared domain/data code across Android and desktop builds.
- Build system: Gradle Kotlin DSL with module-level `build.gradle.kts` files and a version catalog in `gradle/libs.versions.toml`.
- Architecture: Expect Kotlin Multiplatform with `commonMain` for shared logic and platform-specific code in `androidMain`/`desktopMain`.
- UX: Prefer Compose UI patterns where present; keep UI state deterministic and testable.
- Data: Keep data models and mappers in shared code when possible; avoid platform leaks into `commonMain`.
- Quality: Favor small, focused changes; add tests where practical and avoid breaking Android/desktop parity.

## What This Project Can Do
- Build Android APKs for the `narada` and `mahati` apps.
- Produce desktop builds via the shared multi-platform setup.
- Share common business logic across platforms in `shared` and `commonMain` sources.
- Support cross-platform feature development with shared domain/data layers.

## Copilot Working Guidelines
- Use existing patterns in `shared/src/commonMain` first; only introduce new dependencies if required.
- Keep platform-specific code behind interfaces or expect/actual when appropriate.
- Avoid touching generated or `build/` outputs.
- Follow existing Gradle and module boundaries; do not move files across modules without a clear reason.

## Available Agents

### Plan
- Purpose: Researches and outlines multi-step plans.
- Use when: The request needs structured planning, sequencing, or a roadmap before implementation.
- Output: A concise, ordered plan that can be followed in subsequent steps.
