# Quick-Fix Workflow

간단한 수정 작업에 사용하는 빠른 워크플로우입니다.

## When to Use

모든 조건을 만족해야 함:
- 수정 파일 ≤ 2개
- 단일 레이어만 영향
- UI 변경 없음
- API/DB 변경 없음
- 새 의존성 없음

## Agent Sequence

```
code-writer → code-critic
```

| Agent | Required | Notes |
|-------|----------|-------|
| code-writer | ✅ | Direct implementation |
| code-critic | ✅ | Quick review |

## Progress Checklist

```
Quick-Fix Progress:
- [ ] Step 1: Implement fix (code-writer)
- [ ] Step 2: Build verification
- [ ] Step 3: Code review (code-critic)
- [ ] Step 4: Final verification
```

## MCP Requirements

| Tool | Requirement | Purpose |
|------|-------------|---------|
| sequential-thinking | Skip | Not needed |
| context7 | Optional | If API reference needed |
| codex-cli | 1 round | Quick review |

## Auto-Upgrade Triggers

다음 상황 발생 시 **feature** 워크플로우로 자동 업그레이드:

| Trigger | Action |
|---------|--------|
| UI changes detected | → feature |
| Files become 3+ | → feature |
| API/DB changes needed | → feature |
| Build fails 2x | → investigate |

## Quality Gates

### Gate 2: Implementation Complete
- [ ] Fix implemented
- [ ] Build succeeds: `./gradlew build`
- [ ] No regression introduced

### Gate 3: Review Complete
- [ ] Quick review passed
- [ ] 0 critical issues
- [ ] ≤2 major issues

## Examples

### Valid Quick-Fix
```
User: "버튼 색상을 빨간색으로 바꿔줘"
→ 1 file (Composable), UI layer only, simple change
→ quick-fix workflow OK
```

### Should Upgrade to Feature
```
User: "버튼 클릭 시 API 호출해서 결과 보여줘"
→ Multiple files, API integration, state management
→ Upgrade to feature workflow
```

## Build Verification

```bash
# Quick verification
./gradlew :feature:home:build

# If module unknown
./gradlew build
```
