# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build
./gradlew build                                    # 전체 빌드
./gradlew :feature:home:build                      # 모듈 빌드
./gradlew clean build                              # 클린 빌드

# Test
./gradlew test                                     # 전체 단위 테스트
./gradlew :feature:home:testDebugUnitTest          # 모듈 단위 테스트
./gradlew :core:domain:test                        # Domain 모듈 테스트
./gradlew test --tests "*.LottoResultViewModelTest" # 단일 테스트 클래스

# Install
./gradlew :app:installDebug                        # 디바이스에 설치
```

## Architecture

한국 로또 관리 앱. Clean Architecture + MVI 패턴, 멀티 모듈 구조.

### Module Structure

```
app/                    → Navigation (Navigation3 type-safe), MainActivity, Application
core/
  domain/               → 순수 Kotlin. Model, UseCase (invoke operator), Repository interface
  data/                 → RepositoryImpl, Mapper, DataSource 조합
  network/              → Retrofit API (동행복권 API: common.do)
  database/             → Room DB (LottoDatabase, v4), Entity, DAO
  design-system/        → Material3 테마, LottoTheme, 공용 컴포넌트 (LottoBall, LottoCard 등)
  util/                 → 유틸리티
  di/                   → Hilt 공통 모듈
feature/
  home/                 → 로또 결과 조회, 수동 입력
  qrscan/               → QR 스캔 (ML Kit + CameraX)
  my-lotto/             → 내 로또 관리
build-logic/convention/ → 커스텀 Gradle 플러그인 (lotto.android.application, lotto.android.library, lotto.android.hilt, lotto.jvm.library)
```

### MVI Contract Pattern

모든 feature는 `{Feature}Contract.kt` → `{Feature}ViewModel.kt` → `{Feature}Screen.kt` 구조를 따름.

```kotlin
// Contract: UiState(data class), Event(sealed interface), Effect(sealed interface)
// ViewModel: _state(MutableStateFlow), state(StateFlow), _effect(Channel), effect(receiveAsFlow)
// Screen: collectAsStateWithLifecycle + LaunchedEffect로 Effect 수집
```

### Layer Dependency Rules

```
✅ feature:* → core:domain, core:design-system, core:di
✅ core:data → core:domain, core:network, core:database
❌ core:domain → core:data, core:network (domain은 외부 의존성 없음)
❌ feature:* → feature:* (feature 간 직접 참조 금지)
```

## Tech Stack

- **Kotlin 2.0.20**, compileSdk 36, minSdk 24
- **Compose** (BOM 2024.12.01) + **Material3**
- **Hilt** 2.52 (DI), **Room** 2.6.1 (DB), **Retrofit** 2.11.0 + **kotlinx.serialization**
- **Navigation3** 1.0.0 (type-safe, @Serializable NavKey)
- **CameraX** 1.4.0 + **ML Kit Barcode** 17.3.0
- **Testing**: JUnit 4, MockK 1.13.13, Turbine 1.2.0, kotlinx-coroutines-test

## Key Conventions

### Naming

| Type | Pattern | Example |
|------|---------|---------|
| UseCase | `{Action}{Subject}UseCase` | `GetLottoResultUseCase` |
| Repository | `{Subject}Repository` / `{Subject}RepositoryImpl` | `LottoRepository` |
| Contract | `{Feature}Contract` (UiState, Event, Effect) | `LottoResultContract` |
| Mapper | `to{Target}()` | `toDomain()`, `toEntity()` |
| Event handler | `on{Action}` | `onRefresh()` |

### Forbidden Patterns

- LiveData → StateFlow 사용
- GlobalScope → viewModelScope 사용
- runBlocking (production) → suspend + coroutines
- XML layouts → Jetpack Compose
- Public mutable collections → immutable interface 노출

### Design System Usage

`core:design-system` 모듈의 `LottoTheme`을 통해 접근:
- Spacing: `LottoTheme.spacing.cardPadding`, `.screenHorizontalPadding` 등
- Colors: `LottoTheme.colors.ballYellow`, `.success`, `.textPrimary` 등
- Components: `LottoBall(number)`, `LottoCard { }`, `LottoButton(onClick) { }`
- Ball 색상: 1-10 Yellow, 11-20 Blue, 21-30 Red, 31-40 Grey, 41-45 Green

## Navigation

Navigation3 type-safe 라우팅. `NavKey`는 `@Serializable sealed interface`로 정의:

```kotlin
// app/navigation/NavKey.kt
NavKey.HomeScreen, NavKey.QrScanScreen, NavKey.MyLottoScreen, NavKey.ManualInputScreen
```

Back stack은 `rememberSaveable` + custom `Saver`로 관리.

## Adding a New Feature

1. **core:domain** — Model, UseCase (`suspend operator fun invoke`), Repository interface
2. **core:data** — RepositoryImpl, Mapper (`toDomain()`, `toEntity()`)
3. **core:network/database** — API endpoint / Entity + DAO
4. **feature:{name}** — Contract, ViewModel, Screen + Content (state hoisting)
5. **app/navigation** — NavKey에 route 추가, NavDisplay에 entry 추가
6. **core:data/di** — `@Binds`로 Repository 바인딩
