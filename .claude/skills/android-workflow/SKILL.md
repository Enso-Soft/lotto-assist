---
name: android-workflow
description: |
  Orchestrates Android development workflows for Kotlin/Compose projects.
  Routes tasks to appropriate workflow: feature, quick-fix, refactor, investigate, hotfix.
  Use when user requests: implementation (구현, 추가, 새 기능, 만들어, 개발),
  bug fixes (버그, 수정, 오류, 고쳐), refactoring (리팩토링, 개선, 정리),
  debugging (분석, 원인, 왜, 디버깅, 안돼), or hotfix (긴급, 프로덕션, 핫픽스).
  Provides Self-Check Protocol and Quality Gates for implementation tasks.
---

# Android Workflow Orchestrator

Android 개발 작업을 적절한 워크플로우로 라우팅하고 품질 게이트를 관리합니다.

## Self-Check Protocol (MUST DO)

구현 작업 시작 전 반드시 확인:

| # | Question | Action if No |
|---|----------|--------------|
| 1 | 이것은 구현 작업인가? | 아니면 직접 처리 |
| 2 | 워크플로우를 분류했는가? | task-router 먼저 호출 |
| 3 | 올바른 순서를 따르고 있는가? | 워크플로우 재확인 |
| 4 | 에이전트를 건너뛰려는가? | 절대 금지, 순서 준수 |

## Quick Classification

### 분류 기준표

| Condition | Workflow |
|-----------|----------|
| Files ≤2, single layer, no UI/API/DB | **quick-fix** |
| New feature, new screen | **feature** |
| Structure change, behavior preserved | **refactor** |
| Unknown bug cause, error analysis | **investigate** |
| Production emergency | **hotfix** |

### 자동 업그레이드 규칙

| Trigger | From | To |
|---------|------|-----|
| UI changes detected | quick-fix | feature |
| Files 3+ | quick-fix | feature |
| API/DB changes | quick-fix | feature |
| Build fails 2x | any | investigate |

## Workflow Sequences

각 워크플로우의 에이전트 실행 순서:

| Workflow | Sequence |
|----------|----------|
| **quick-fix** | code-writer → code-critic |
| **feature** | planner → [ux-engineer] → [ui-builder] → code-writer → test-engineer → code-critic |
| **refactor** | planner → code-writer → test-engineer → code-critic |
| **investigate** | investigator → (route based on findings) |
| **hotfix** | code-writer → test-engineer(smoke) → code-critic |

**상세 가이드:**
- quick-fix: [workflows/quick-fix.md](workflows/quick-fix.md)
- feature: [workflows/feature.md](workflows/feature.md)
- refactor: [workflows/refactor.md](workflows/refactor.md)
- investigate: [workflows/investigate.md](workflows/investigate.md)
- hotfix: [workflows/hotfix.md](workflows/hotfix.md)

## Quality Gates

| Gate | Checkpoint | Criteria |
|------|------------|----------|
| Gate 0 | task-router | Classification complete |
| Gate 1 | planner | Requirements clear, tasks defined |
| Gate 2 | code-writer | Build succeeds |
| Gate 3 | code-critic | 0 critical, ≤2 major issues |

**상세 기준:** [gates/quality-gates.md](gates/quality-gates.md)

## MCP Tool Requirements

| Workflow | sequential-thinking | context7 | codex-cli |
|----------|---------------------|----------|-----------|
| quick-fix | Skip | Optional | 1 round |
| feature | 3+ steps | Required | 2+ rounds |
| refactor | 3+ steps | Optional | 2+ rounds |
| investigate | 5+ steps | Optional | Optional |
| hotfix | Skip | Optional | 1 round |

## Build Commands

```bash
# Full build
./gradlew build

# Module build
./gradlew :feature:home:build

# Run tests
./gradlew test
./gradlew :feature:home:testDebugUnitTest

# Clean build
./gradlew clean build
```

## Failure Recovery

빌드 또는 게이트 실패 시:

1. **Record**: 에러 메시지, 환경 정보 기록
2. **Retry**: 자동 재시도 1회
3. **Escalate**: 여전히 실패 시 옵션 제시
   - A: 수동 수정 후 재시도
   - B: investigator 호출
   - C: 작업 중단
