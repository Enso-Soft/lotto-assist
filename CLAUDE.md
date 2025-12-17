# CLAUDE.md
Guidance for Claude Code (claude.ai/code) in this repository.

**Language**: Always respond in Korean (한국어) unless explicitly asked otherwise.

## Non-negotiable rules (CRITICAL)

### 0) If unsure, ask first (NO GUESSING)
- Do **not** invent APIs/classes/files. If something is unclear or missing, **ask questions** before coding.
- If a requirement can be interpreted multiple ways, propose options and ask which one to implement.

### 1) Read & Search before editing
- Before changing a file: **open/read it first**. Never modify unseen code.
- Before creating new code: **grep/search** for similar implementations and reuse patterns.

### 2) Minimal change
- Touch only what’s required for the request.
- No drive-by refactors, renames, formatting-only changes, or unrelated cleanup.

### 3) Clean Architecture boundaries (strict)
- **Domain**: pure Kotlin, **no Android/framework deps**.
- **Data**: implements Domain repositories, depends on Domain only.
- **Presentation**: Compose UI + ViewModel only. No business logic beyond UI orchestration.

### 4) Compose + Material 3 (strict)
- Use `MaterialTheme.colorScheme`, `typography`, `shapes`.
- No hardcoded colors. Avoid hardcoded sizes; prefer theme/dimens when repeated.
- UI text: resources (`strings.xml`). (Exception: internal debug-only strings.)

### 5) MVI contract (strict)
- `UiState`: immutable `data class`
- `UiEvent`: `sealed class/interface`
- `UiEffect`: one-shot via `Channel` + `receiveAsFlow()`
- State: private mutable, public read-only `StateFlow`.

---

## Project summary
**Lotto Assist**: Lotto number management/analysis Android app.
Stack: Kotlin, Jetpack Compose, Hilt, Coroutines/Flow, Room, Retrofit.
Architecture: Clean Architecture + MVI. Features are micro-modules.

## Module layout (high level)
- `app/`: application + navigation host
- `core/`
    - `domain/`: models, repository interfaces, usecases
    - `data/`: repository implementations, mappers
    - `network/`: Retrofit APIs + DTOs
    - `database/`: Room entities/DAOs
    - `di/`: Hilt modules, dispatchers
    - `util/`: shared utilities
- `feature/`
    - `home/`
    - `qrscan/`

---

## Required workflow for any task

### Step A) Clarify
Ask questions when needed. Especially:
- Sorting rules (tie-breakers), default ordering, paging behavior
- UX details (BottomSheet contents, copy, empty/error states)
- Data source truth (DB vs network vs cached)

### Step B) Explore
- Locate existing patterns (MVI, repository, bottom sheets, list rendering).
- Identify exact files/modules to change.

### Step C) Plan (must show a short plan)
- Outline changes by layer/module.
- Mention new/changed types (State/Event/Effect, UseCase, Repo methods).
- List risks/assumptions.

### Step D) Implement incrementally
- Implement smallest vertical slice first (domain → data → presentation only if needed).
- Keep changes localized.

### Step E) Verify (mandatory)
Run commands and fix until green:
- `./gradlew test`
- `./gradlew lint` (if configured) or `./gradlew check`
- `./gradlew assembleDebug`
  If instrumentation is relevant:
- `./gradlew connectedAndroidTest` (only when device/emulator is available)

**If you cannot run commands** (CI/tooling limits): say so explicitly and provide exact commands + expected outcomes.

---

## Build / Test commands (canonical)

```bash
# Build
./gradlew clean build
./gradlew assembleDebug

# Unit tests
./gradlew test
./gradlew :core:domain:test

# Quality gate
./gradlew check

# Instrumentation (only if device/emulator available)
./gradlew connectedAndroidTest
```

## Implementation conventions

### Domain
- UseCases: VerbNounUseCase, operator fun invoke(...).
- Keep logic single-responsibility.
- Return Result<T> for failure propagation (preferred), or Flow<Result<T>> when streaming.

### Data
- Map DTO/Entity → Domain inside Data.
- Main-safe: switch dispatcher inside repository (inject dispatcher via Hilt).
- No Android framework types in Data unless isolated to database/network modules.

### Presentation (Compose)
- Screens should be stateless; state lives in ViewModel.
- Use collectAsStateWithLifecycle().
- Effects collected in LaunchedEffect(Unit).

### Resources
- User-facing strings -> strings.xml
- Repeated sizes -> dimens.xml (optional)
- Custom colors -> colors.xml (but prefer Material 3 theme first)

--- 

## Lotto-specific rules

### Official API (dhlottery)
- Base: https://www.dhlottery.co.kr/
- Draw result: /common.do?method=getLottoNumber&drwNo={drawNo}

### Ball color ranges
- 1–10 Yellow
- 11–20 Blue
- 21–30 Red
- 31–40 Gray
- 41–45 Green
- Use theme-friendly colors (consider dark mode contrast).

### QR parsing
- Implemented under feature/qrscan.
- Validate: 1–45 range + no duplicates.

---

## Forbidden patterns

**Never**
- God objects
- Storing Activity/Context long-term
- Blocking main thread with IO/DB/network
- GlobalScope
- Manual singletons (use Hilt)

**Avoid**
- Deprecated APIs
- Hardcoded strings/colors
- Duplicate code, unnecessary abstraction

---

## Definition of Done (must satisfy)
- No architecture boundary violations.
- No invented APIs/classes; everything compiles.
- Build + unit tests green (or explicit note why cannot run locally).
- UI follows Material 3 + resources.
- Sorting/filtering logic has deterministic tie-breakers.
- Edge cases handled: loading/empty/error states.