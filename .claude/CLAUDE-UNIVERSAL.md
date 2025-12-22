# CLAUDE.md (Universal Template)

> **Version**: 3.0.0
> **Updated**: 2025-12-23
> **Type**: Universal (All Platforms)

This file provides guidance to Claude Code (claude.ai/code) when working with code in any project.

---

## 1. Core AI Instructions

- **Ask First**: When uncertain, ask clarifying questions before proceeding
- **Read Before Write**: Always read files/context before making modifications
- **Minimal Changes**: Only modify what's necessary for the task
- **Priority Order**: User instructions > Project rules > General rules
- **Context Management**: Use TASKS.md for long-horizon tasks

---

## 2. Platform Detection

Claude will automatically detect the project platform by examining:

1. **Build files**: `build.gradle.kts`, `package.json`, `Podfile`, `requirements.txt`, etc.
2. **Source directories**: `src/`, `app/`, `ios/`, `android/`, etc.
3. **Configuration files**: `tsconfig.json`, `Cargo.toml`, `go.mod`, etc.

Once detected, load the corresponding platform profile from `.claude/platforms/`.

### Manual Override

If auto-detection fails, specify in TASKS.md:
```markdown
- **Platform**: android | ios | web-frontend | web-backend | cross-platform
```

---

## 3. Agent System

### 3.1 Agent Tiers

| Tier | Agents | Description |
|------|--------|-------------|
| **Tier 1: Core** | task-router, planner, code-writer, code-critic | Essential for all workflows |
| **Tier 2: Extended** | test-engineer, investigator, performance-optimizer | Task-specific |
| **Tier 3: Domain** | ux-engineer, ui-builder | UI/UX work only |

### 3.2 Model Assignment

| Model | Use Case | Agents |
|-------|----------|--------|
| **haiku** | Fast classification, simple tasks | task-router |
| **sonnet** | General implementation, analysis | planner, code-writer, test-engineer, investigator, ux-engineer, ui-builder, performance-optimizer |
| **opus** | Deep analysis, critical reviews | code-critic |

---

## 4. Workflow Templates

### 4.1 Task Router (Entry Point)

All tasks start with `task-router` to determine the optimal workflow.

```
User Request → task-router → Workflow Selection → Agent Execution
```

### 4.2 Quick-Fix Workflow

**Path**: `task-router → code-writer → code-critic`

**Conditions**:
- Changed files ≤ 2
- Single layer impact
- No UI changes
- No external API/DB changes

**MCP Policy**: sequential-thinking ❌, context7 ⭕, codex-cli 1 round

### 4.3 Feature Workflow

**Path**: `task-router → planner → [ux-engineer] → [ui-builder] → code-writer → test-engineer → code-critic → [performance-optimizer]`

**Conditions**:
- New feature/screen
- Multi-layer impact
- Complex requirements

**MCP Policy**: sequential-thinking ✅ 3+, context7 ✅, codex-cli 2+ rounds

### 4.4 Refactor Workflow

**Path**: `task-router → planner → code-writer → test-engineer → code-critic`

**Conditions**:
- Structure improvement
- Behavior preserved
- No UI changes

**MCP Policy**: sequential-thinking ✅ 3+, context7 ⭕, codex-cli 2+ rounds

### 4.5 Investigate Workflow

**Path**: `task-router → investigator → (route based on findings)`

**Conditions**:
- Unknown bug cause
- Unclear reproduction steps
- Complex debugging needed

**MCP Policy**: sequential-thinking ✅ 5+, context7 ⭕, codex-cli ⭕

### 4.6 Hotfix Workflow

**Path**: `task-router → code-writer → test-engineer(smoke) → code-critic`

**Conditions**:
- Production emergency
- Immediate fix required

**MCP Policy**: Minimal (speed priority)

### 4.7 Auto-Upgrade Rules

| Trigger | From | To |
|---------|------|-----|
| UI changes detected | quick-fix | feature |
| Files 3+ | quick-fix | feature |
| API/DB changes | quick-fix | feature |
| Build fails 2x | any | investigate |

---

## 5. MCP Policy

### 5.1 Timeout & Retry

| MCP Server | Timeout | Retry | Purpose |
|------------|---------|-------|---------|
| sequential-thinking | 60s | 1 | Complex analysis, planning |
| context7 | 30s | 1 | Library documentation lookup |
| codex-cli | 120s | 2 | Code discussion, review |
| exa | 30s | 1 | Web search |
| github | 30s | 1 | Issue/PR lookup |

### 5.2 Conditional Usage Matrix

| Task Type | sequential-thinking | context7 | codex-cli |
|-----------|---------------------|----------|-----------|
| **quick-fix** | ❌ Skip | ⭕ Optional | ⭕ 1 round |
| **feature** | ✅ 3+ steps | ✅ Required | ✅ 2+ rounds |
| **refactor** | ✅ 3+ steps | ⭕ Optional | ✅ 2+ rounds |
| **investigate** | ✅ 5+ steps | ⭕ Optional | ⭕ Optional |
| **hotfix** | ❌ Skip | ⭕ Optional | ⭕ 1 round |

### 5.3 Fallback Strategy

| MCP Server | Fallback Action |
|------------|-----------------|
| sequential-thinking | Use native analysis |
| context7 | Skip with warning |
| codex-cli | Single round only |
| exa/github | Skip (optional MCP) |

### 5.4 Display Format

- [Sequential] Step N: {analysis content}
- [Context7] {library}: {lookup purpose}
- [Codex] Round N: {discussion summary}
- [Fallback] {MCP}: {fallback action}

---

## 6. SuperClaude Integration

### 6.1 Flag → MCP Mapping

| Flag | MCP Behavior |
|------|--------------|
| `--think` | sequential-thinking 3+ steps |
| `--think-hard` | sequential-thinking 5+ steps |
| `--ultrathink` | sequential-thinking 8+ steps + branching |
| `--c7` / `--context7` | context7 auto-enabled |
| `--validate` | Build/test verification required |
| `--safe-mode` | Extended gates + change approval |
| `--delegate` | Sub-agent parallel execution |
| `--no-mcp` | All MCP disabled |

### 6.2 Default Flags per Agent

| Agent | Default Flags |
|-------|---------------|
| task-router | (none) |
| planner | `--think-hard`, `--c7` |
| code-writer | `--think`, `--c7`, `--validate` |
| code-critic | `--think-hard`, `--loop` |
| test-engineer | `--think`, `--c7` |
| investigator | `--ultrathink` |
| performance-optimizer | `--think-hard`, `--validate` |

### 6.3 User Override

User-specified flags override defaults:
```
User: "Analyze this bug with --ultrathink"
→ investigator uses --ultrathink instead of default
```

---

## 7. Quality Gates

### 7.1 Checkpoint System

| CP | Name | Guard Condition | Exit Criteria | On Failure |
|----|------|-----------------|---------------|------------|
| **CP-0** | Init | Task received | Requirements documented | Ask user |
| **CP-1** | Plan | Scope agreed | Plan approved | Retry 1x |
| **CP-2** | Impl | Plan exists | Build success | Retry 1x |
| **CP-3** | Test | Impl complete | Tests pass | Investigate |
| **CP-4** | Review | Tests pass | No critical issues | Fix & retry |
| **CP-5** | Complete | Review pass | User approval | N/A |

### 7.2 Quality Thresholds

| Item | Threshold |
|------|-----------|
| Build success | Required (100%) |
| Critical issues | 0 |
| Major issues | ≤ 2 |
| Test pass rate | 80%+ (configurable in platform profile) |

### 7.3 Failure Recovery Protocol

```
1. Record failure (error message, environment info)
2. Classify (environment / build / logic / dependency)
3. Auto-retry (1x)
4. Auto-fix attempt (1 hypothesis)
5. Isolate + Ask user:
   - A: Manual fix then retry
   - B: Invoke investigator
   - C: Abort task
```

---

## 8. TASKS.md System

### 8.1 Template

```markdown
# Task: [Name]

## Overview
- **Type**: quick-fix | feature | refactor | investigate | hotfix
- **Platform**: [auto-detected or specified]
- **Status**: IN_PROGRESS | COMPLETE
- **Current Agent**: [agent-name]

## Checkpoints
- [x] CP-0: Init
- [x] CP-1: Plan
- [ ] CP-2: Impl (in progress)
- [ ] CP-3: Test
- [ ] CP-4: Review
- [ ] CP-5: Complete

## Decisions
| Decision | Reason | Date |
|----------|--------|------|

## Files Modified
- path/to/file1
- path/to/file2

## Notes
- [Issues, open questions]
```

### 8.2 Session Resume Protocol

```
1. READ TASKS.md FIRST
2. IDENTIFY current checkpoint & agent
3. DETECT platform from project or TASKS.md
4. LOAD platforms/{platform}.yaml
5. DECIDE next action
6. INVOKE appropriate agent with platform context
```

---

## 9. Platform Context Injection

Agents use the `{{PLATFORM_CONTEXT}}` placeholder, which is replaced at runtime with:

```yaml
# From .claude/platforms/{platform}.yaml
architecture:
  pattern: [Architecture Pattern]
  layers: [Layer definitions]

build:
  commands:
    build: [Build Command]
    test: [Test Command]
    lint: [Lint Command]

testing:
  unit:
    framework: [Test Framework]
  coverage_target: [Coverage %]

preferred_patterns: [Best Practices]
forbidden_patterns: [Anti-patterns]
```

---

## 10. Success Metrics

### Efficiency Metrics

| Metric | Baseline | Target | Measurement |
|--------|----------|--------|-------------|
| Quick-fix avg time | 60s | 20s | task-router start → code-critic complete |
| Feature avg time | 10min | 7min | planner start → last agent complete |
| Rework rate | 30% | 15% | code-critic → code-writer recall ratio |

### Cost Metrics

| Metric | Baseline | Target | Measurement |
|--------|----------|--------|-------------|
| opus usage ratio | 57% | 11% (1/9) | Per-agent model assignment |
| MCP mandatory calls | 100% | Conditional | Task type based |

### Quality Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Gate first-pass rate | 80%+ | Gate pass/fail records |
| Build success rate | 95%+ | Post code-writer build results |

---

## 11. File Structure

```
.claude/
├── CLAUDE.md                    # This file (or CLAUDE-UNIVERSAL.md)
├── platforms/                   # Platform profiles
│   ├── android.yaml
│   ├── ios.yaml
│   ├── web-frontend.yaml
│   └── web-backend.yaml
├── agents/                      # Universal agent templates
│   ├── task-router.md
│   ├── planner.md
│   ├── code-writer.md
│   ├── test-engineer.md
│   ├── code-critic.md
│   ├── investigator.md
│   ├── ux-engineer.md
│   ├── ui-builder.md
│   └── performance-optimizer.md
└── skills/                      # Detailed guidelines (reference)
    └── [platform-patterns].md
```

---

## 12. Priority Rules

```
CLAUDE.md (Core Rules)
    ↓ Override
platforms/*.yaml (Platform Rules)
    ↓ Override
TASKS.md (Task-specific Decisions)
```

---

> **Version History**
> - 3.0.0: Universal platform support, SuperClaude integration, 9 agents
> - 2.0.0: Workflow templates, MCP policy, new agents
> - 1.0.0: Initial version (Android-specific)
