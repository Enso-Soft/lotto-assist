# Feature Workflow

새로운 기능이나 화면을 구현할 때 사용하는 워크플로우입니다.

## When to Use

- 새로운 화면 추가
- 새로운 기능 구현
- UI 변경이 포함된 작업
- API 또는 DB 스키마 변경이 필요한 작업

## Agent Sequence

```
planner → [ux-engineer] → [ui-builder] → code-writer → test-engineer → code-critic
```

| Agent | Required | Condition |
|-------|----------|-----------|
| planner | ✅ | Always |
| ux-engineer | ⭕ | If UI changes |
| ui-builder | ⭕ | If new components needed |
| code-writer | ✅ | Always |
| test-engineer | ✅ | Always |
| code-critic | ✅ | Always |

## Progress Checklist

Copy and track progress:

```
Feature Implementation Progress:
- [ ] Step 1: Requirements analysis (planner)
- [ ] Step 2: UX design review (ux-engineer) - if UI changes
- [ ] Step 3: UI component design (ui-builder) - if new components
- [ ] Step 4: Implementation (code-writer)
- [ ] Step 5: Test writing (test-engineer)
- [ ] Step 6: Code review (code-critic)
- [ ] Step 7: Quality gate verification
```

## MCP Requirements

| Tool | Requirement | Purpose |
|------|-------------|---------|
| sequential-thinking | 3+ steps | Requirements analysis, design |
| context7 | Required | API/library documentation |
| codex-cli | 2+ rounds | Code review discussion |

## Quality Gates

### Gate 1: Planning Complete
- [ ] Requirements documented
- [ ] Affected layers identified
- [ ] Task breakdown complete
- [ ] Dependencies identified

### Gate 2: Implementation Complete
- [ ] All code written
- [ ] Build succeeds: `./gradlew build`
- [ ] No compilation errors
- [ ] Architecture patterns followed

### Gate 3: Review Complete
- [ ] Tests written and passing
- [ ] 0 critical issues
- [ ] ≤2 major issues
- [ ] Code review approved

## Example

```
User: "로또 결과 분석 기능 추가해줘"

1. planner: 요구사항 분석, 레이어별 작업 분해
2. ux-engineer: 분석 화면 UX 설계 (UI 변경 있음)
3. ui-builder: 차트 컴포넌트 설계 (새 컴포넌트 필요)
4. code-writer: Domain/Data/Presentation 구현
5. test-engineer: ViewModel, UseCase 테스트 작성
6. code-critic: 코드 리뷰 및 품질 검증
```
