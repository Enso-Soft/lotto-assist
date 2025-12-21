# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**Language**: Always respond in Korean (한국어) unless explicitly asked otherwise.

---

## Task Tracking (TASKS.md)

**IMPORTANT**: Always check and update `TASKS.md` before starting any task. Write TASKS.md content in Korean.

- **Before starting**: Read TASKS.md to understand current progress
- **During work**: Add current task to "In Progress" section
- **After completion**: Move to "Completed" section with checkmark

---

## MCP Server Usage Guide

This project leverages the following MCP servers to support efficient development:

### Context7 (Code Context Search)
**Purpose**: Search for up-to-date documentation, architecture patterns, and existing implementations in the project

**When to use**:
- Before implementing new features, to explore similar patterns
- To verify how architecture layers interact with each other
- To understand the complete implementation flow of a specific feature
- To check naming conventions and code style

**Usage example**: `"Search for existing ViewModel MVI pattern implementations"`

### Sequential Thinking (Structured Problem Solving)
**Purpose**: Decompose complex tasks into logical steps for systematic approach

**When to use**:
- When implementing complex features spanning multiple architecture layers
- When analyzing dependencies between multiple modules/layers
- When analyzing impact scope for performance optimization, refactoring, etc.
- When requirements are ambiguous or multi-step decision-making is needed

**Usage example**: `"Add new QR scan feature: step-by-step analysis from Domain → Data → Presentation"`

**Integrated workflow**:
1. Use **Sequential Thinking** to decompose problems and establish a plan
2. Use **Context7** to search for existing patterns/implementations
3. **Step-by-Step implementation** (refer to Required workflow below)

**IMPORTANT**: If you did not use or skipped MCP tools, you must explicitly explain the reason.
- e.g., "Simple single-file modification, Sequential Thinking unnecessary"
- e.g., "Completely new pattern, designing directly instead of Context7 search"
- e.g., "Already read the file and understood the pattern"

**Example workflow**: "Add filtering feature to winning number history screen"
1. Sequential Thinking → Plan layers (Domain: FilterCriteria, Data: Repo param, Presentation: UI State)
2. Context7 → Search patterns ("BottomSheet filter UI", "UiState immutable update")
3. Implement → Domain → Data → Presentation, verify at each step

### Exa (Web Search / Research)
**Purpose**: Collect up-to-date external information to support implementation and troubleshooting

**When to use**:
- Before coding, to identify expected pitfalls and common failure modes
- To verify external API usage, breaking changes, or release notes
- To find real-world solutions for errors or stack traces
- To compare patterns and best practices across sources

**Notes**:
- Use Exa to draft a pre-flight troubleshooting checklist
- Always validate findings against local versions and the current codebase

### GitHub MCP (Issue/PR/Repo)
**Purpose**: Automate GitHub issue/PR/repository work and keep an audit trail

**Account**: `Enso-Soft`

**Repository** `lotto-assist`

**When to use**:
- Read issues/PRs and inspect details (e.g., body/comments/status)
- Create/update issues and add comments
- Check PR status/reviews and list changed files
- Automate simple file updates, branch creation, or PR creation

**Usage example**:
- "Read issue #123 under the Enso-Soft account"
- "Create a bug report issue and add labels"
- "List changed files in PR #45 and leave review comments"

### Codex CLI MCP (AI Code Analysis)
**Purpose**: Project structure analysis, code review, improvement suggestions

**Tools**: `codex` (analysis), `review` (code review), `ping`, `help`

**⚠️ CRITICAL: Debate-based usage required**

Do NOT accept Codex analysis as-is. Must conduct **Claude ↔ Codex debate**.

**Required 3-round process**:
1. **Round 1**: Codex initial analysis → receive suggestions
2. **Round 2**: Claude challenges → "Verified in code?", "Over-engineering?", "Fits current scale?"
3. **Round 3**: Final consensus → prioritize (P0-P4), filter out impractical items

**Key questions to ask**:
- "Did you verify this in actual code?"
- "Is this necessary for current project scale?"
- "Does build/test actually fail?"

**Prohibited**:
- ❌ Documenting Codex results without debate
- ❌ Accepting all suggestions without challenge
- ❌ Skipping code verification

**Output**: Document debate results in `IMPROVE.md`

---

## Non-negotiable rules (CRITICAL)

### 0) If unsure, ask first (NO GUESSING)
- Do **not** invent APIs/classes/files. If something is unclear or missing, **ask questions** before coding.
- If a requirement can be interpreted multiple ways, propose options and ask which one to implement.

### 0-1) MCP usage declaration (REQUIRED)
- You must state whether you utilized **Context7** or **Sequential Thinking** MCP during the task.
- If you did not use MCP, you **must explain the reason** (e.g., "Simple 1-line fix, MCP unnecessary").

### 0-2) MCP usage enforcement (NO SKIP FOR NON-TRIVIAL TASKS)
- **Default: run Sequential Thinking for every task**. Only skip when it is a truly trivial 1–2 line fix; if you skip, state the reason explicitly in the response.
- If the task changes UI behavior, interaction patterns, or spans multiple steps, **you must run Sequential Thinking first** and show a short plan before editing.
- If you believe MCP is unnecessary, **ask for explicit confirmation** before proceeding and record the reason in the response.

### 0-3) Context7 required for API usage changes
- When introducing or changing framework/library APIs (e.g., Compose modifiers, animations, paging/snap), **run Context7 first** to confirm the latest recommended APIs and deprecations.
- If Context7 is not used, **stop and ask for explicit approval** to proceed without it.

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
- Search for existing patterns (MVI, repository, bottom sheets, list rendering)
- Identify exact files/modules to change
- Verify similar implementations exist and reuse them

### Step C) Plan (must show a short plan)
- Outline changes by layer/module
- Mention new/changed types (State/Event/Effect, UseCase, Repo methods)
- List risks/assumptions
- Identify dependencies between layers

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

## Issue-Based Workflow (Reusable)
1. Read the issue body and summarize requirements + open decisions
2. Ask clarifying questions and confirm decisions (save mode, duplicate rule, multi-round allowance, UX copy, data source, etc.)
3. Update the issue body with final decisions and leave a comment explaining the rationale/history
4. Confirm base branch and create a `feature/*` branch from it
5. Explore existing patterns (MVI, repo/DAO, UI reuse) and note exact files to touch
6. Implement minimal changes in order: Domain → Data → Presentation
7. Move any UI text to resources; keep Material 3 and theme usage
8. Run tests/builds; if not possible, state the exact commands to run
9. Commit changes (confirm whether to include `AGENTS.md` updates)
10. Push branch to origin
11. Create PR with correct base/head and include `Closes #issue-number`
12. Review the PR, leave review comments if needed, fix issues, re-run tests, and commit
13. Post test results as a PR comment
14. End the flow only when no further fixes remain and tests/builds are green

---

## Build / Test commands

```bash
./gradlew clean build
./gradlew assembleDebug
./gradlew test
./gradlew :core:domain:test
./gradlew check
./gradlew connectedAndroidTest  # Only if device/emulator available
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

### QR parsing
- Implemented under feature/qrscan
- Validate: 1–45 range + no duplicates
- Ball colors (1-10: Yellow, 11-20: Blue, 21-30: Red, 31-40: Gray, 41-45: Green) - use theme-friendly colors

---

## Forbidden patterns

- God objects, storing Activity/Context long-term
- Blocking main thread with IO/DB/network, GlobalScope
- Manual singletons (use Hilt), deprecated APIs
- Hardcoded strings/colors, duplicate code, unnecessary abstraction

---

## Definition of Done (must satisfy)
- No architecture boundary violations.
- No invented APIs/classes; everything compiles.
- Build + unit tests green (or explicit note why cannot run locally).
- UI follows Material 3 + resources.
- Sorting/filtering logic has deterministic tie-breakers.
- Edge cases handled: loading/empty/error states.
