# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

Lotto Assist는 로또 번호 관리 및 분석을 위한 Android 애플리케이션입니다. Kotlin 기반으로 Clean Architecture와 MVI 패턴을 적용하며, Jetpack Compose, Hilt, Coroutine, Flow, Room, Retrofit 등 최신 Android 기술 스택을 사용합니다.

### 모듈 구조

```
app/                    # 메인 애플리케이션 모듈
core/
  ├── data/            # Repository 구현체
  ├── database/        # Room 데이터베이스
  ├── di/              # Hilt 의존성 주입
  ├── domain/          # UseCase, Entity, Repository 인터페이스
  ├── network/         # Retrofit API
  └── util/            # 공통 유틸리티
feature/
  └── home/            # Home 화면 기능
```

## 빌드 커맨드

### 기본 빌드 작업

```bash
# 전체 프로젝트 빌드
./gradlew build

# Debug APK 빌드
./gradlew assembleDebug

# Release APK 빌드
./gradlew assembleRelease

# 빌드 산출물 정리
./gradlew clean
```

### 테스트

```bash
# 모든 유닛 테스트 실행
./gradlew test

# 연결된 기기에서 instrumented 테스트 실행
./gradlew connectedAndroidTest

# 특정 모듈 테스트 실행
./gradlew :core:domain:test
```

### 코드 품질

```bash
# 모든 검사 실행 (lint, 테스트 등)
./gradlew check
```

## 아키텍처 원칙

### 계층 구조

```
Presentation (UI) → Domain → Data
```

**의존성 규칙**: 안쪽 계층은 바깥쪽 계층을 알지 않습니다. Domain이 핵심이며, Presentation과 Data는 Domain에 의존합니다.

### 각 계층의 역할

**Domain 계층**
- UseCase, Entity, Repository 인터페이스 포함
- 순수 Kotlin 코드로 구성 (Android 프레임워크 의존 금지)
- 비즈니스 로직의 단일 진실 원천

**Data 계층**
- Repository 구현체, DataSource, DTO, Entity 매핑
- Retrofit API 통신, Room 데이터베이스 처리
- DTO/Entity → Domain 모델 변환은 이 계층에서 수행

**Presentation 계층**
- Composable, ViewModel
- UI 표현과 사용자 상호작용만 담당
- 비즈니스 로직 직접 구현 금지

## MVI 패턴 구현

### 상태 관리 구조

```kotlin
// State - 불변 데이터 클래스
data class UiState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val error: String? = null
)

// Event - sealed class로 정의
sealed class UiEvent {
    data class LoadData(val id: String) : UiEvent()
    object Refresh : UiEvent()
}

// Effect - 일회성 이벤트 (Navigation, Toast 등)
sealed class UiEffect {
    data class ShowError(val message: String) : UiEffect()
    object NavigateBack : UiEffect()
}
```

### ViewModel 구현 패턴

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val useCase: MyUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: UiEvent) {
        when (event) {
            is UiEvent.LoadData -> loadData(event.id)
            is UiEvent.Refresh -> refresh()
        }
    }

    private fun loadData(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            useCase(id)
                .onSuccess { data ->
                    _state.update { it.copy(isLoading = false, data = data) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
```

## Jetpack Compose 가이드

### 상태 호이스팅

Composable은 stateless하게 작성하고, 상태는 ViewModel에서 관리합니다.

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    MyContent(
        state = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun MyContent(
    state: UiState,
    onEvent: (UiEvent) -> Unit
) {
    // UI 구현
}
```

### Effect 처리

```kotlin
LaunchedEffect(Unit) {
    viewModel.effect.collect { effect ->
        when (effect) {
            is UiEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            is UiEffect.NavigateBack -> navController.popBackStack()
        }
    }
}
```

## Coroutine & Flow 원칙

### Dispatcher 사용

- **Dispatchers.IO**: 네트워크, DB 작업
- **Dispatchers.Default**: CPU 연산
- **Dispatchers.Main**: UI 조작

**Main-safe 원칙**: 모든 suspend 함수는 내부에서 적절한 Dispatcher로 전환하여 호출 측에서 안전하게 사용할 수 있도록 합니다.

### Dispatcher 주입

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
```

### Flow 수집

```kotlin
// Compose에서
val state by flow.collectAsStateWithLifecycle()

// ViewModel에서
viewModelScope.launch {
    repository.getDataFlow()
        .flowOn(Dispatchers.IO)
        .catch { e -> _state.update { it.copy(error = e.message) } }
        .collectLatest { data -> _state.update { it.copy(data = data) } }
}
```

## 데이터 계층 구현

### Repository 패턴

```kotlin
// Domain에 인터페이스 정의
interface UserRepository {
    suspend fun getUser(id: Long): Result<User>
    fun getUserFlow(id: Long): Flow<User>
}

// Data에 구현체
class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: UserLocalDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {

    override suspend fun getUser(id: Long): Result<User> = withContext(ioDispatcher) {
        runCatching {
            val dto = remoteDataSource.fetchUser(id)
            localDataSource.saveUser(dto.toEntity())
            dto.toDomain()
        }
    }
}
```

### DTO/Entity 매핑

매핑 로직은 Data 계층에 위치합니다.

```kotlin
// Data 계층에서 정의
fun UserDto.toDomain(): User = User(
    id = this.id,
    name = this.fullName,
    profileUrl = this.avatar
)

fun UserEntity.toDomain(): User = User(
    id = this.id,
    name = this.name,
    profileUrl = this.avatarUrl
)

fun User.toEntity(): UserEntity = UserEntity(
    id = this.id,
    name = this.name,
    avatarUrl = this.profileUrl
)
```

## 의존성 주입 (Hilt)

### 모듈 구성

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
}

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
```

### ViewModel 주입

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel()
```

## 코드 스타일

### 네이밍 규칙

- 클래스/인터페이스: `PascalCase`
- 함수/변수: `camelCase`
- 상수: `UPPER_SNAKE_CASE`
- 축약형 사용 금지

### 필수 준수 사항

- `val` 우선 사용 (불변성)
- data class는 `copy()`를 통한 불변 패턴 유지
- public API 리턴 타입 명시
- 단일 책임 원칙 준수

## 금지 패턴

### 절대 금지

- **God Object**: 모든 것을 관리하는 거대 클래스
- **Context 장기 보관**: Activity Context를 프로퍼티에 저장 (메모리 누수)
- **메인 스레드 차단**: 네트워크/DB 작업을 메인에서 실행
- **GlobalScope 사용**: 구조적 동시성 위반
- **수동 싱글톤**: DI 대신 static 변수로 객체 관리

### 지양 사항

- Deprecated API 사용 (AsyncTask, startActivityForResult 등)
- 하드코딩된 문자열/색상/치수
- 불필요한 주석 (코드로 의도 표현)
- 중복 코드 (DRY 원칙 위반)
- 과도한 추상화 (YAGNI 원칙)

## 테스트 고려사항

### 테스트 용이성 확보

- 생성자 주입으로 의존성 교체 가능하게 설계
- Dispatcher 주입으로 테스트 시 `TestDispatcher` 사용
- Repository는 인터페이스로 정의하여 Fake 구현 가능

### 계층별 테스트

- **Domain/UseCase**: 순수 JUnit 테스트
- **ViewModel**: `TestDispatcher` + Mock Repository
- **UI**: Compose Testing 라이브러리
