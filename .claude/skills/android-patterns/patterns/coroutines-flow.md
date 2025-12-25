# Coroutines & Flow Patterns

Kotlin Coroutines와 Flow 사용 패턴 가이드입니다.

## Coroutines Basics

### CoroutineScope in ViewModel

```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val useCase: SomeUseCase
) : ViewModel() {

    fun loadData() {
        // viewModelScope: ViewModel lifecycle에 바인딩
        viewModelScope.launch {
            val result = useCase()
            // Handle result
        }
    }

    // 여러 작업 병렬 실행
    fun loadMultipleData() {
        viewModelScope.launch {
            val deferred1 = async { useCase1() }
            val deferred2 = async { useCase2() }

            val result1 = deferred1.await()
            val result2 = deferred2.await()
        }
    }
}
```

### Dispatcher Usage

```kotlin
// IO 작업 (네트워크, DB)
withContext(Dispatchers.IO) {
    repository.fetchData()
}

// CPU 집약적 작업
withContext(Dispatchers.Default) {
    processLargeData(data)
}

// UI 작업 (일반적으로 불필요 - viewModelScope는 Main)
withContext(Dispatchers.Main) {
    updateUI()
}
```

## Flow Patterns

### StateFlow for UI State

```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor() : ViewModel() {

    // Private mutable
    private val _uiState = MutableStateFlow(UiState())

    // Public immutable
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Update state
    fun updateState() {
        _uiState.update { currentState ->
            currentState.copy(isLoading = true)
        }
    }
}
```

### Channel for One-Time Events

```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor() : ViewModel() {

    // Channel for effects
    private val _effect = Channel<Effect>()
    val effect = _effect.receiveAsFlow()

    // Send effect
    fun showToast(message: String) {
        viewModelScope.launch {
            _effect.send(Effect.ShowToast(message))
        }
    }
}

// UI에서 수집
@Composable
fun FeatureScreen(viewModel: FeatureViewModel) {
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowToast -> {
                    // Show toast
                }
            }
        }
    }
}
```

### Flow from Repository

```kotlin
// Repository
fun getItemsStream(): Flow<List<Item>> =
    dao.observeAll()
        .map { entities -> entities.map { it.toDomain() } }

// ViewModel
private val _items = MutableStateFlow<List<Item>>(emptyList())
val items: StateFlow<List<Item>> = _items.asStateFlow()

init {
    viewModelScope.launch {
        repository.getItemsStream()
            .collect { items ->
                _items.value = items
            }
    }
}
```

### stateIn for Flow to StateFlow

```kotlin
// Repository Flow를 StateFlow로 변환
val items: StateFlow<List<Item>> = repository.getItemsStream()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
```

## Error Handling

### Result Pattern

```kotlin
// UseCase
suspend operator fun invoke(): Result<Data> =
    runCatching {
        repository.getData()
    }

// ViewModel
viewModelScope.launch {
    useCase()
        .onSuccess { data ->
            _uiState.update { it.copy(data = data) }
        }
        .onFailure { error ->
            _uiState.update { it.copy(error = error.message) }
        }
}
```

### fold Pattern

```kotlin
useCase().fold(
    onSuccess = { data ->
        _uiState.update { it.copy(isLoading = false, data = data) }
    },
    onFailure = { error ->
        _uiState.update { it.copy(isLoading = false, error = error.message) }
        _effect.send(Effect.ShowError(error.message ?: "Unknown error"))
    }
)
```

### Flow Error Handling

```kotlin
repository.getDataStream()
    .catch { error ->
        emit(emptyList())  // Emit default value
        _effect.send(Effect.ShowError(error.message))
    }
    .collect { data ->
        _uiState.update { it.copy(data = data) }
    }
```

## Cancellation

### Structured Concurrency

```kotlin
// 자식 coroutine은 부모와 함께 취소됨
viewModelScope.launch {
    val job = launch {
        // This will be cancelled when viewModelScope is cancelled
        delay(Long.MAX_VALUE)
    }

    // Or manually cancel
    job.cancel()
}
```

### SupervisorJob

```kotlin
// 하나의 자식 실패가 다른 자식에 영향 안 줌
viewModelScope.launch {
    supervisorScope {
        launch { task1() }  // 실패해도
        launch { task2() }  // 계속 실행
    }
}
```

## Testing

### runTest

```kotlin
@Test
fun `loadData should update state`() = runTest {
    // Given
    coEvery { useCase() } returns Result.success(mockData)

    // When
    viewModel.loadData()

    // Then - advanceUntilIdle waits for all coroutines
    advanceUntilIdle()

    assertEquals(mockData, viewModel.uiState.value.data)
}
```

### Turbine for Flow Testing

```kotlin
@Test
fun `state should emit loading then data`() = runTest {
    viewModel.uiState.test {
        // Initial state
        assertEquals(UiState(), awaitItem())

        // Trigger load
        viewModel.loadData()

        // Loading state
        assertEquals(UiState(isLoading = true), awaitItem())

        // Data loaded
        val finalState = awaitItem()
        assertFalse(finalState.isLoading)
        assertEquals(mockData, finalState.data)

        cancelAndIgnoreRemainingEvents()
    }
}
```

## Anti-Patterns

### DON'T

```kotlin
// ❌ GlobalScope 사용
GlobalScope.launch { ... }

// ❌ runBlocking in production
runBlocking { ... }

// ❌ Flow를 collect 없이 사용
flow.map { ... }  // 실행되지 않음

// ❌ Main thread에서 blocking 작업
suspend fun fetchData() {
    val data = api.getData()  // Should be withContext(Dispatchers.IO)
}
```

### DO

```kotlin
// ✅ viewModelScope 사용
viewModelScope.launch { ... }

// ✅ IO dispatcher for blocking operations
withContext(Dispatchers.IO) {
    api.getData()
}

// ✅ Flow를 항상 collect
flow.collect { ... }

// ✅ 또는 stateIn으로 변환
flow.stateIn(viewModelScope, ...)
```
