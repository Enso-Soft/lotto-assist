# Investigate Workflow

원인을 알 수 없는 버그나 문제를 분석할 때 사용합니다.

## When to Use

- 버그 원인 불명확
- 에러 메시지만으로 원인 파악 어려움
- "왜 안돼?" / "Why not working?"
- 재현 조건 불명확

## Agent Sequence

```
investigator → (route based on findings)
```

| Phase | Agent | Purpose |
|-------|-------|---------|
| Analysis | investigator | Root cause analysis |
| Resolution | varies | Based on findings |

## Progress Checklist

```
Investigation Progress:
- [ ] Step 1: Reproduce the issue
- [ ] Step 2: Collect error logs and stack traces
- [ ] Step 3: Identify affected components
- [ ] Step 4: Analyze root cause
- [ ] Step 5: Determine fix complexity
- [ ] Step 6: Route to appropriate workflow
- [ ] Step 7: Verify fix resolves issue
```

## MCP Requirements

| Tool | Requirement | Purpose |
|------|-------------|---------|
| sequential-thinking | 5+ steps (ultrathink) | Deep analysis |
| context7 | Optional | Reference documentation |
| codex-cli | Optional | Discuss findings |

## Investigation Process

### Step 1: Reproduce
```
- Can you reproduce the issue?
- What are the exact steps?
- Is it consistent or intermittent?
```

### Step 2: Collect Data
```
- Error messages
- Stack traces
- Log outputs
- Environment info
```

### Step 3: Analyze
```
- When did it start?
- What changed recently?
- Which component is failing?
- What's the expected vs actual behavior?
```

### Step 4: Root Cause
```
- What is the actual cause?
- Is it a code bug, config issue, or external factor?
- How confident are you in the diagnosis?
```

### Step 5: Route to Fix

| Finding | Route To |
|---------|----------|
| Simple bug, clear fix | quick-fix |
| Complex bug, multiple changes | feature |
| Architecture issue | refactor |
| External/config issue | manual fix |

## Quality Gates

### Gate: Investigation Complete
- [ ] Issue reproduced
- [ ] Root cause identified
- [ ] Fix approach determined
- [ ] Appropriate workflow selected

## Examples

### Example 1: Build Error
```
User: "빌드가 안돼요, 왜 그럴까요?"

Investigation:
1. Reproduce: ./gradlew build → fails
2. Collect: Error message, stack trace
3. Analyze: Missing dependency? Syntax error?
4. Root cause: Kotlin version mismatch
5. Route: quick-fix (gradle config change)
```

### Example 2: Runtime Crash
```
User: "앱이 갑자기 죽어요"

Investigation:
1. Reproduce: Launch app → crash
2. Collect: Logcat, crash report
3. Analyze: NullPointerException in HomeViewModel
4. Root cause: Uninitialized state
5. Route: quick-fix (null safety fix)
```

### Example 3: Complex Issue
```
User: "데이터가 가끔 안 보여요"

Investigation:
1. Reproduce: Intermittent, hard to reproduce
2. Collect: Network logs, state snapshots
3. Analyze: Race condition in data loading
4. Root cause: Coroutine scope issue
5. Route: feature (proper state management)
```

## Build Commands

```bash
# Check build status
./gradlew build --info

# Run with stacktrace
./gradlew build --stacktrace

# Clean and rebuild
./gradlew clean build
```
