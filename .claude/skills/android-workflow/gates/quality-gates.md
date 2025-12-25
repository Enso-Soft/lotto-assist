# Quality Gates

모든 워크플로우에 적용되는 품질 게이트 상세 기준입니다.

## Gate Overview

| Gate | Checkpoint | Owner | Criteria |
|------|------------|-------|----------|
| Gate 0 | Classification | task-router | Workflow determined |
| Gate 1 | Planning | planner | Requirements clear |
| Gate 2 | Implementation | code-writer | Build succeeds |
| Gate 3 | Review | code-critic | Quality approved |

## Gate 0: Classification

**Owner**: task-router

### Pass Criteria
- [ ] Task type identified (feature/quick-fix/refactor/investigate/hotfix)
- [ ] Workflow sequence determined
- [ ] MCP requirements identified
- [ ] Risk level assessed

### Output
```json
{
  "classification": "feature",
  "workflow": ["planner", "code-writer", "test-engineer", "code-critic"],
  "mcp_requirements": {...},
  "risk_level": "medium"
}
```

## Gate 1: Planning

**Owner**: planner

### Pass Criteria
- [ ] Requirements clearly documented
- [ ] Affected layers identified (Domain/Data/Presentation)
- [ ] Task breakdown complete
- [ ] Dependencies identified
- [ ] Risks assessed

### Output
```markdown
## Requirements
- [Clear requirements list]

## Affected Layers
- Domain: [changes]
- Data: [changes]
- Presentation: [changes]

## Tasks
1. [Task 1]
2. [Task 2]
...
```

## Gate 2: Implementation

**Owner**: code-writer

### Pass Criteria
- [ ] All code written
- [ ] Build succeeds
- [ ] No compilation errors
- [ ] Architecture patterns followed
- [ ] Code compiles without warnings (or warnings documented)

### Build Verification
```bash
# Must pass
./gradlew build

# Or module-specific
./gradlew :feature:home:build
```

### Failure Actions
| Failure Type | Action |
|--------------|--------|
| Compilation error | Fix and retry |
| Build fails 2x | Escalate to investigate |
| Dependency issue | Check versions |

## Gate 3: Review

**Owner**: code-critic

### Issue Classification

| Level | Symbol | Criteria | Threshold |
|-------|--------|----------|-----------|
| Critical | 🔴 | Security risk, data loss, crash | **0 allowed** |
| Major | 🟠 | Bug, performance issue, bad pattern | **≤2 allowed** |
| Minor | 🟡 | Code smell, style issue | No limit |
| Suggestion | 🟢 | Nice to have | No limit |

### Pass Criteria
- [ ] 0 critical issues (🔴)
- [ ] ≤2 major issues (🟠)
- [ ] All issues have suggested fixes
- [ ] Architecture alignment verified
- [ ] Security concerns addressed

### Review Checklist

#### Code Quality
- [ ] Clear naming
- [ ] Appropriate abstractions
- [ ] Single responsibility
- [ ] Low coupling, high cohesion

#### Architecture
- [ ] Layer separation maintained
- [ ] Dependencies point inward
- [ ] Clean Architecture principles

#### Security
- [ ] Input validation
- [ ] No hardcoded secrets
- [ ] No injection risks

#### Performance
- [ ] No obvious inefficiencies
- [ ] Proper async patterns
- [ ] Memory management

### Verdicts

| Verdict | Criteria | Action |
|---------|----------|--------|
| ✅ Approved | 0 critical, ≤2 major | Proceed |
| ⚠️ Conditional | Minor issues only | Proceed with notes |
| ❌ Rejected | Critical or >2 major | Return to code-writer |

## Feedback Loop

```
code-writer ──────→ code-critic
     ↑                   │
     │   ❌ Rejected     │
     └───────────────────┘
```

### Rejection Process
1. code-critic documents issues
2. code-writer receives feedback
3. code-writer applies fixes
4. code-critic re-reviews
5. Maximum 2 iterations before escalation

## Metrics

### Success Metrics
| Metric | Target |
|--------|--------|
| Gate first-pass rate | 80%+ |
| Build success rate | 95%+ |
| Review approval rate | 85%+ |

### Tracking
- Record pass/fail for each gate
- Track rework rate
- Monitor build times
