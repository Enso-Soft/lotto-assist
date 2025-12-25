# AGENTS.md (feature)

## SlotMachineNumber 컴포넌트 안내

- 위치: `core/design-system/src/main/java/com/enso/designsystem/component/SlotMachineNumber.kt`
- 목적: 회차 숫자 등에서 **카운트 기반 롤링(슬롯머신) 애니메이션** 제공

### 동작 특성
- 전체 숫자 변화량을 기준으로 각 자릿수의 회전 스텝을 계산합니다.
- 동일한 자리 숫자라도 카운트 스텝만큼 회전합니다.
- 자릿수 증가도 동일한 드럼 애니메이션으로 처리됩니다.
- 드럼 높이는 `displaySmall`의 `lineHeight` 기준으로 계산합니다.

### 사용 시 주의
- 숫자와 함께 배치되는 텍스트는 동일한 텍스트 스타일을 사용해 세로 정렬을 맞춥니다.
- `textStyle`, `textColor`로 숫자 스타일과 색상을 지정할 수 있습니다.
