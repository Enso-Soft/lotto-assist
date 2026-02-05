# Repository Guidelines

## Project Structure & Module Organization
`lotto-assist` is a multi-module Android project using Clean Architecture + MVI.

- `app/`: application entry, navigation, top-level wiring
- `core/domain`: pure domain models, repository interfaces, use cases
- `core/data`: repository implementations, data-source orchestration, mappers
- `core/network`: Retrofit APIs and DTOs
- `core/database`: Room entities/DAOs and DB config
- `core/di`: Hilt modules and dependency bindings
- `core/design-system`, `core/util`: shared UI and utilities
- `feature/home`, `feature/qrscan`, `feature/my-lotto`: feature-specific UI/ViewModel logic
- Tests are under each module’s `src/test` (and `src/androidTest` where applicable).

## Build, Test, and Development Commands
Use Gradle wrapper from the repo root.

- `./gradlew assembleDebug`: build debug APK for local validation
- `./gradlew test`: run all unit tests across modules
- `./gradlew check`: run verification tasks (includes tests/lint checks configured by modules)
- `./gradlew :core:data:test`: run tests for one module only (fast feedback)
- `./gradlew connectedAndroidTest`: instrumentation tests on a connected device/emulator

## Coding Style & Naming Conventions
- Language: Kotlin + Jetpack Compose, 4-space indentation, keep functions/classes focused.
- Follow module boundaries strictly: Domain has no Android dependencies.
- Naming:
  - Use cases: `VerbNounUseCase` (e.g., `SyncLottoResultsUseCase`)
  - Repository interfaces in Domain, `*RepositoryImpl` in Data
  - MVI contracts: immutable `UiState`, sealed `UiEvent`/`UiEffect`
- UI text must be in `strings.xml`; avoid hardcoded user-facing strings.

## Testing Guidelines
- Primary stack: JUnit4, MockK, Coroutines Test, Turbine.
- Test names should describe behavior (Korean/English both used in this repo).
- Add tests for synchronization edge cases (partial failure, retries, missing rounds) when changing data sync logic.

## Commit & Pull Request Guidelines
- Recent history shows concise Korean messages and occasional scoped `refactor:` style.
- Recommended commit style: short imperative summary, optionally with scope (e.g., `core-data: fix missing round sync`).
- PRs should include:
  - What changed and why
  - Affected modules
  - Test/build results (`test`, `check`, `assembleDebug`)
  - Linked issue (`Closes #<number>`) when relevant
  - Screenshots/GIFs for UI-visible changes
