# Refactor Workflow

코드 구조를 개선하면서 동작은 유지하는 리팩토링 작업에 사용합니다.

## When to Use

- 코드 구조 변경 (동작 유지)
- 아키텍처 패턴 적용
- 코드 정리 및 개선
- 기술 부채 해소

## Agent Sequence

```
planner → code-writer → test-engineer → code-critic
```

| Agent | Required | Purpose |
|-------|----------|---------|
| planner | ✅ | Refactoring scope and plan |
| code-writer | ✅ | Apply refactoring |
| test-engineer | ✅ | Verify behavior preserved |
| code-critic | ✅ | Quality validation |

## Progress Checklist

```
Refactoring Progress:
- [ ] Step 1: Scope analysis (planner)
- [ ] Step 2: Identify affected code
- [ ] Step 3: Plan refactoring steps
- [ ] Step 4: Apply refactoring (code-writer)
- [ ] Step 5: Run existing tests
- [ ] Step 6: Add/update tests (test-engineer)
- [ ] Step 7: Code review (code-critic)
- [ ] Step 8: Verify behavior preserved
```

## MCP Requirements

| Tool | Requirement | Purpose |
|------|-------------|---------|
| sequential-thinking | 3+ steps | Plan refactoring safely |
| context7 | Optional | Pattern references |
| codex-cli | 2+ rounds | Validate changes |

## Refactoring Principles

### DO
- Small, incremental changes
- Run tests after each change
- Preserve existing behavior
- Update tests to match new structure

### DON'T
- Change behavior during refactoring
- Skip testing between changes
- Refactor and add features simultaneously
- Break working code

## Quality Gates

### Gate 1: Planning Complete
- [ ] Scope clearly defined
- [ ] Affected files identified
- [ ] Refactoring steps planned
- [ ] Risk assessment done

### Gate 2: Refactoring Complete
- [ ] All changes applied
- [ ] Build succeeds
- [ ] Existing tests pass
- [ ] No behavior changes

### Gate 3: Review Complete
- [ ] Code review approved
- [ ] 0 critical issues
- [ ] Architecture improved
- [ ] Tests updated

## Examples

### Valid Refactoring
```
User: "HomeViewModel을 MVI 패턴으로 리팩토링해줘"
→ Structure change, behavior preserved
→ refactor workflow
```

### Should Be Feature
```
User: "리팩토링하면서 새 기능도 추가해줘"
→ Behavior change included
→ Separate into: refactor first, then feature
```

## Build Verification

```bash
# Run all tests before refactoring
./gradlew test

# Verify after each change
./gradlew build

# Final verification
./gradlew clean build test
```
