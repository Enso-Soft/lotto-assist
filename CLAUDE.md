# CLAUDE.md

> **Version**: 4.0.1
> **Updated**: 2025-12-24
> **Platform**: Android (Kotlin)
> **Changes**: Serena MCP 사용 가이드 추가

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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

## 2. Skills System

워크플로우, 코딩 패턴, 테스트 가이드, 컨벤션은 Skills로 관리됩니다.
키워드 기반 자동 트리거로 필요한 지식이 주입됩니다.

| Skill | 트리거 키워드 | 내용 |
|-------|--------------|------|
| `android-workflow` | 구현, 버그, 리팩토링, 분석, 긴급 | 워크플로우 오케스트레이션 |
| `android-patterns` | MVI, 아키텍처, 컴포즈, 코루틴 | 코딩 패턴 가이드 |
| `android-testing` | 테스트, TDD, 커버리지, 목 | 테스트 템플릿 |
| `android-conventions` | 네이밍, 컨벤션, 스타일 | 코딩 규칙 |

```
.claude/skills/
├── android-workflow/     # 워크플로우 (feature, quick-fix, refactor, investigate, hotfix)
├── android-patterns/     # MVI, Clean Architecture, Compose, Coroutines
├── android-testing/      # JUnit5, MockK, Turbine, Compose Test
└── android-conventions/  # 네이밍, 금지패턴, 권장패턴
```

## 3. Platform Profile (Android)

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

## 4. MCP Policy

### Timeout & Retry

| MCP Server | Timeout | Retry | Purpose |
|------------|---------|-------|---------|
| sequential-thinking | 60s | 1 | Complex analysis, planning |
| context7 | 30s | 1 | Library documentation lookup |
| codex-cli | 120s | 2 | Code discussion, review |
| exa | 30s | 1 | Web search |
| github | 30s | 1 | Issue/PR lookup |
| serena | 30s | 1 | Symbolic code navigation/editing |

### Fallback Strategy

| MCP Server | Fallback Action |
|------------|-----------------|
| sequential-thinking | Use native analysis |
| context7 | Skip with warning |
| codex-cli | Single round only |
| exa/github | Skip (optional MCP) |
| serena | Native file/edit tools |

## 5. Project Architecture

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

## References

| Resource | Location |
|----------|----------|
| Universal Specification | `.claude/CLAUDE-UNIVERSAL.md` |
| Platform Profiles | `.claude/platforms/*.yaml` |
| Agent Templates | `.claude/agents/*.md` |
| Skills | `.claude/skills/` |
