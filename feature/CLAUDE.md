# CLAUDE.md (feature)

feature 모듈 공통 컴포넌트 중 슬롯머신 숫자 애니메이션은 아래를 기준으로 사용합니다.

## SlotMachineNumber

- 위치: `core/design-system/src/main/java/com/enso/designsystem/component/SlotMachineNumber.kt`
- 목적: 회차 숫자 등 **숫자 카운트 기반 슬롯머신 롤링** 표시

### 사용 예시

```kotlin
SlotMachineNumber(
    targetNumber = selectedResult?.round ?: 0,
    totalDurationMs = 200
) {}
```

### 동작 요약
- 전체 숫자 변화량을 기준으로 각 자리의 회전 스텝을 계산합니다.
- 1의 자리도 값이 같더라도 카운트 스텝만큼 회전합니다.
- 자릿수 증가(예: 999 → 1000)도 기존 드럼 애니메이션과 동일하게 등장합니다.
- 드럼 높이는 `displaySmall`의 `lineHeight`(없으면 `fontSize`) 기준으로 계산합니다.

### 파라미터
- `targetNumber`: 목표 숫자
- `totalDurationMs`: 한 자리의 전체 롤링 기본 시간
- `textStyle`: 숫자 텍스트 스타일(기본: displaySmall + ExtraBold)
- `textColor`: 숫자 텍스트 색상(기본: LocalLottoColors.textMainLight)

### 주의사항
- 숫자 텍스트는 `MaterialTheme.typography.displaySmall` + `FontWeight.ExtraBold`에 맞춰 사용합니다.
- 주변 텍스트와 세로 정렬이 필요하면 동일한 텍스트 스타일을 사용하세요.
