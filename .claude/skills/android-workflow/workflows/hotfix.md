# Hotfix Workflow

프로덕션 긴급 장애 대응에 사용하는 빠른 워크플로우입니다.

## When to Use

- 프로덕션 환경 장애
- 긴급 수정 필요
- 사용자에게 즉시 영향
- 시간이 매우 제한적

## Agent Sequence

```
code-writer → test-engineer(smoke) → code-critic
```

| Agent | Required | Notes |
|-------|----------|-------|
| code-writer | ✅ | Minimal fix |
| test-engineer | ✅ | Smoke tests only |
| code-critic | ✅ | Quick security/quality check |

## Progress Checklist

```
Hotfix Progress:
- [ ] Step 1: Identify critical issue
- [ ] Step 2: Implement minimal fix (code-writer)
- [ ] Step 3: Smoke test (test-engineer)
- [ ] Step 4: Quick review (code-critic)
- [ ] Step 5: Deploy verification
- [ ] Step 6: Schedule proper fix (if needed)
```

## MCP Requirements

| Tool | Requirement | Purpose |
|------|-------------|---------|
| sequential-thinking | Skip | No time |
| context7 | Optional | Quick reference |
| codex-cli | 1 round | Quick validation |

## Hotfix Principles

### DO
- Fix the immediate issue only
- Minimal code changes
- Quick smoke testing
- Document for follow-up

### DON'T
- Refactor during hotfix
- Add new features
- Make unnecessary changes
- Skip security review

## Quality Gates

### Gate 2: Fix Complete
- [ ] Minimal fix applied
- [ ] Build succeeds
- [ ] Core functionality works
- [ ] No new security issues

### Gate 3: Quick Review
- [ ] No critical issues
- [ ] Security validated
- [ ] Ready for deployment

## Smoke Test Checklist

최소한의 테스트:

```
- [ ] App launches
- [ ] Critical path works
- [ ] Fix resolves issue
- [ ] No obvious regression
```

## Post-Hotfix Actions

핫픽스 후 반드시:

1. **Document**: 무엇을, 왜 수정했는지 기록
2. **Schedule**: 적절한 수정을 위한 후속 작업 예약
3. **Review**: 근본 원인 분석
4. **Prevent**: 재발 방지 대책 수립

## Examples

### Production Crash
```
User: "프로덕션에서 앱이 죽어요! 긴급 수정 필요!"

1. code-writer: Null check 추가 (최소 수정)
2. test-engineer: 앱 실행, 크래시 없음 확인
3. code-critic: 보안 이슈 없음 확인
4. 배포 후 proper fix 예약
```

### Data Loss Risk
```
User: "사용자 데이터가 삭제되고 있어요!"

1. code-writer: 삭제 로직 비활성화
2. test-engineer: 데이터 유지 확인
3. code-critic: 사이드 이펙트 검토
4. 배포 후 근본 원인 분석
```

## Build Commands

```bash
# Quick build
./gradlew assembleRelease

# Quick test
./gradlew testReleaseUnitTest

# Deploy verification
./gradlew build
```
