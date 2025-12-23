# CLAUDE.md

> **Version**: 3.1.0
> **Updated**: 2025-12-23
> **Platform**: Android (Kotlin)
> **Changes**: Workflow enforcement rules, self-check protocol added

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Quick Reference

```
.claude/
├── CLAUDE-UNIVERSAL.md      # Full universal specification
├── platforms/
│   ├── android.yaml         # ← This project uses this
│   ├── ios.yaml
│   ├── web-frontend.yaml
│   └── web-backend.yaml
└── agents/                  # 10 universal agents
    ├── task-router.md       # Entry point - classifies tasks
    ├── planner.md           # Requirements & planning
    ├── ux-engineer.md       # UX design
    ├── ui-builder.md        # UI components
    ├── code-writer.md       # Implementation
    ├── test-engineer.md     # Testing
    ├── code-critic.md       # Code review
    ├── investigator.md      # Bug analysis
    ├── performance-optimizer.md
    └── github-master.md     # GitHub issues/PR management
```

## 1. Core AI Instructions

- **Language**: Always respond in Korean
- **Memory Efficiency**: Use specialized sub-agents for complex tasks
- **Tool Optimization**: Evaluate results before proceeding to next steps
- **Parallel Processing**: Run independent tasks concurrently
- **Verification**: Always verify critical operations
- **Context Management**: Use TASKS.md for long-horizon tasks

### Agent Delegation Rules (MUST)

| Task Type | Delegate To | Direct MCP Usage |
|-----------|-------------|------------------|
| GitHub Issues/PR | `github-master` | ❌ Forbidden |
| Code Implementation | `code-writer` | - |
| Code Review | `code-critic` | - |
| Bug Analysis | `investigator` | - |

**IMPORTANT**: When an agent exists for a task domain, the main agent MUST NOT call MCP tools directly. Always delegate through the appropriate agent.

### Workflow Enforcement Rules (CRITICAL)

#### Never Skip Rule
All implementation tasks MUST follow this sequence:

```
1. Receive implementation request
2. [REQUIRED] Call task-router → Classify workflow
3. [REQUIRED] Execute agents in sequence per classified workflow
4. [FORBIDDEN] Skip agents (except those marked optional)
5. [FORBIDDEN] Direct code modification (only code-writer agent can modify code)
```

#### Self-Check Protocol (Required Before Every Task)

Before starting any implementation task, answer these questions:

| # | Question | Action if No |
|---|----------|--------------|
| 1 | Is this an implementation task? | If not implementation, direct handling OK |
| 2 | Has task-router been called? | Call task-router first |
| 3 | Am I following the correct workflow? | Re-verify workflow |
| 4 | Am I about to skip an agent? | Never skip, follow sequence |

#### Quick-Fix vs Feature Classification Criteria

| Condition | quick-fix | feature |
|-----------|-----------|---------|
| Files to modify | ≤2 | 3+ |
| UI changes | ❌ None | ✅ Yes |
| API/DB changes | ❌ None | ✅ Yes |
| New dependencies | ❌ None | ✅ Yes |
| Layers affected | Single | Multi |

**If ANY feature condition is met → Use feature workflow**

#### User Response Interpretation Rules

| User Response | Meaning | Skip Workflow? |
|---------------|---------|----------------|
| "Yes", "Do it", "OK" | Approval to proceed | ❌ No |
| "Do it quickly" | Speed request | ❌ No |
| "Skip the workflow" | Explicit skip request | ⚠️ Warn and confirm |

#### Violation Recovery Procedure

```
1. Stop direct modification immediately
2. Re-classify task with task-router
3. Restart with correct workflow
4. Consider reverting previous direct changes
```

## 2. Success Metrics

### Efficiency Metrics

| Metric | Baseline | Target | Measurement |
|--------|----------|--------|-------------|
| Quick-Fix avg time | 60s | 20s | task-router start → code-critic complete |
| Feature avg time | 10min | 7min | planner start → last agent complete |
| Rework rate | 30% | 15% | code-critic → code-writer recall ratio |

### Cost Metrics

| Metric | Baseline | Target | Measurement |
|--------|----------|--------|-------------|
| opus usage ratio | 57% (4/7) | 10% (1/10) | Per-agent model assignment |
| MCP mandatory calls | 100% | Conditional | Task type based |

### Quality Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Gate first-pass rate | 80%+ | Gate pass/fail records |
| Build success rate | 95%+ | Post code-writer build results |

## 3. Platform Profile (Android)

This project uses the **Android** platform profile. Key configuration:

| Aspect | Configuration |
|--------|---------------|
| Language | Kotlin 2.0 |
| UI Framework | Jetpack Compose + Material3 |
| Architecture | Clean Architecture + MVI |
| DI | Hilt |
| Testing | JUnit5, MockK, Turbine, Compose UI Test |

### Build Commands

```bash
./gradlew build                           # Full build
./gradlew :feature:home:build             # Module build
./gradlew test                            # All tests
./gradlew :feature:home:testDebugUnitTest # Module tests
./gradlew clean build                     # Clean build
```

## 4. Agent System

### 4.1 Available Agents

| Agent | Model | Description |
|-------|-------|-------------|
| `task-router` | haiku | Task classification, workflow routing |
| `planner` | opus | Requirements analysis, task breakdown |
| `ux-engineer` | sonnet | UX design, screen specifications |
| `ui-builder` | sonnet | Reusable UI component creation |
| `code-writer` | sonnet | Feature implementation |
| `test-engineer` | sonnet | Test writing (Unit/UI/Integration) |
| `code-critic` | opus | Code review, quality validation |
| `investigator` | sonnet | Bug analysis, debugging |
| `performance-optimizer` | sonnet | Performance optimization |
| `github-master` | sonnet | GitHub issue/PR management, workflow integration |

### 4.2 Model Assignment

| Model | Purpose | Agents                                                                                                  |
|-------|---------|---------------------------------------------------------------------------------------------------------|
| **haiku** | Fast classification | task-router                                                                                             |
| **sonnet** | Implementation, analysis | ux-engineer, ui-builder, code-writer, test-engineer, investigator, performance-optimizer, github-master |
| **opus** | Deep review, critical analysis | planner, code-critic                                                                                    |

## 5. Workflow Templates

### 5.1 Workflow Selection

All tasks start with `task-router` for classification:

```
User Request → task-router → Workflow Selection → Agent Execution
```

### 5.2 Available Workflows

| Classification | Workflow | Condition |
|----------------|----------|-----------|
| **quick-fix** | code-writer → code-critic → [github-master] | Files ≤2, single layer, no UI/API/DB |
| **feature** | planner → [ux-engineer] → [ui-builder] → code-writer → test-engineer → code-critic → [github-master] | New feature/screen |
| **refactor** | planner → code-writer → test-engineer → code-critic → [github-master] | Structure change |
| **investigate** | investigator → (route based on findings) → [github-master] | Unknown bug cause |
| **hotfix** | code-writer → test-engineer(smoke) → code-critic → [github-master] | Production emergency |
| **github-driven** | github-master(analyze) → task-router → [workflow] → github-master(output) | GitHub issue/PR based work |
| **github-only** | github-master | GitHub issue/PR creation, analysis, management (standalone) |

> **Note**: `[github-master]` is optional - activated when `--github` flag is used or GitHub reference detected.
> **Note**: `github-only` is used for standalone GitHub operations such as issue creation/analysis.

### 5.3 Auto-Upgrade Rules

| Trigger | From | To |
|---------|------|-----|
| UI changes detected | quick-fix | feature |
| Files 3+ | quick-fix | feature |
| API/DB changes | quick-fix | feature |
| Build fails 2x | any | investigate |
| GitHub issue/PR referenced | any | + github-master |

## 6. MCP Policy

### 6.1 Timeout & Retry

| MCP Server | Timeout | Retry | Purpose |
|------------|---------|-------|---------|
| sequential-thinking | 60s | 1 | Complex analysis, planning |
| context7 | 30s | 1 | Library documentation lookup |
| codex-cli | 120s | 2 | Code discussion, review |
| exa | 30s | 1 | Web search |
| github | 30s | 1 | Issue/PR lookup |

### 6.2 Conditional Usage

| Workflow | sequential-thinking | context7 | codex-cli | github |
|----------|---------------------|----------|-----------|--------|
| quick-fix | Skip | Optional | 1 round | Optional |
| feature | 3+ steps | Required | 2+ rounds | Optional |
| refactor | 3+ steps | Optional | 2+ rounds | Optional |
| investigate | 5+ steps | Optional | Optional | Optional |
| hotfix | Skip | Optional | 1 round | Optional |
| github-driven | 3+ steps | Optional | Optional | Required |

### 6.3 Fallback Strategy

| MCP Server | Fallback Action |
|------------|-----------------|
| sequential-thinking | Use native analysis |
| context7 | Skip with warning |
| codex-cli | Single round only |
| exa/github | Skip (optional MCP) |

### 6.4 Display Format

- [Sequential] Step N: {analysis content}
- [Context7] {library}: {lookup purpose}
- [Codex] Round N: {discussion summary}
- [Fallback] {MCP}: {fallback action}

## 7. SuperClaude Integration

### 7.1 Flag → MCP Mapping

| Flag | Behavior |
|------|----------|
| `--think` | sequential-thinking 3+ steps |
| `--think-hard` | sequential-thinking 5+ steps |
| `--ultrathink` | sequential-thinking 8+ steps + branching |
| `--c7` | Enable context7 |
| `--validate` | Force build/test verification |
| `--no-mcp` | Disable all MCP |
| `--github` | Enable github-master in workflow |

### 7.2 Default Flags per Agent

| Agent | Default Flags |
|-------|---------------|
| task-router | (none) |
| planner | `--think-hard`, `--c7` |
| code-writer | `--think`, `--c7`, `--validate` |
| code-critic | `--think-hard`, `--loop` |
| test-engineer | `--think`, `--c7` |
| investigator | `--ultrathink` |
| performance-optimizer | `--think-hard`, `--validate` |
| github-master | `--think` |

### 7.3 User Override

User-specified flags override defaults:
```
User: "Analyze this bug with --ultrathink"
→ investigator uses --ultrathink instead of default
```

## 8. Quality Gates

| Gate | Checkpoint | Criteria |
|------|------------|----------|
| Gate 0 | task-router | Classification complete |
| Gate 1 | planner | Requirements clear, tasks defined |
| Gate 2 | code-writer | Build succeeds |
| Gate 3 | code-critic | 0 critical, ≤2 major issues |

### Thresholds

- Build success: Required (100%)
- Critical issues: 0
- Major issues: ≤2
- Test pass rate: 80%+

### Failure Recovery

```
1. Record failure (error message, environment info)
2. Auto-retry (1x)
3. If still fails, present options:
   - A: Manual fix then retry
   - B: Invoke investigator
   - C: Abort task
```

## 9. TASKS.md System

### Session Resume Protocol

```
1. READ TASKS.md → Check status
2. IDENTIFY current step
3. DECIDE: COMPLETE → new task | in_progress → resume | pending → start
4. INVOKE appropriate agent
```

### Structure

```markdown
# Task: [Name]

## Overview
- **Type**: quick-fix | feature | refactor | investigate | hotfix
- **Status**: IN_PROGRESS | COMPLETE
- **Current Agent**: [agent-name]

## Workflow
- [x] task-router → quick-fix
- [ ] code-writer → in progress

## Files Modified
- path/to/file.kt

## Decisions
- [Key decisions]
```

## 10. Project Architecture

### Module Structure

```
app/                    # Application entry point
build-logic/            # Convention plugins
core/
├── domain/            # Pure Kotlin - business logic, models, use cases
├── data/              # Repository implementations, data sources
├── network/           # Retrofit API, network models
├── database/          # Room database, DAOs, entities
├── di/                # Hilt DI modules
├── util/              # Utilities
feature/
├── home/              # Main screen with Compose UI, MVI pattern
├── qrscan/            # QR code scanning with CameraX + ML Kit
```

### Key Patterns

- **MVI Architecture**: `*Contract.kt` (UiState/Event/Effect), `*ViewModel.kt` (StateFlow + Channel)
- **Repository Pattern**: Interfaces in domain, implementations in data
- **Dependency Injection**: Hilt with `@HiltViewModel`

### Tech Stack

- Kotlin 2.0, Compose with Material3
- Hilt for DI, Room for local DB, Retrofit/OkHttp for network
- CameraX + ML Kit for QR scanning
- Testing: JUnit5, MockK, Turbine, Coroutines Test

---

## Universal System

For full universal specification applicable to any platform, see:
- `.claude/CLAUDE-UNIVERSAL.md` - Complete universal template
- `.claude/platforms/*.yaml` - Platform-specific configurations
- `.claude/agents/*.md` - Universal agent templates

Each agent uses `{{PLATFORM_CONTEXT}}` placeholder which gets replaced with platform-specific content from the appropriate YAML profile.
