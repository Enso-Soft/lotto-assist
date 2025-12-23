# MVI Pattern Guide

Model-View-Intent 패턴의 상세 구현 가이드입니다.

## Overview

MVI는 단방향 데이터 흐름을 제공하는 아키텍처 패턴입니다.

```
┌─────────────────────────────────────────────┐
│                    UI                       │
│  (Compose Screen/Content)                   │
└─────────────────┬───────────────────────────┘
                  │ Event
                  ▼
┌─────────────────────────────────────────────┐
│               ViewModel                     │
│  (Process Events, Update State)             │
└─────────────────┬───────────────────────────┘
                  │ State / Effect
                  ▼
┌─────────────────────────────────────────────┐
│                    UI                       │
│  (Render State, Handle Effects)             │
└─────────────────────────────────────────────┘
```

## Contract Structure

### File: `{Feature}Contract.kt`

```kotlin
object HomeContract {
    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val lottoResults: List<LottoResult> = emptyList(),
        val selectedRound: Int? = null
    )

    sealed interface Event {
        data object LoadLatest : Event
        data class SelectRound(val round: Int) : Event
        data class Search(val query: String) : Event
        data object Refresh : Event
    }

    sealed interface Effect {
        data class ShowToast(val message: String) : Effect
        data class Navigate(val route: String) : Effect
        data object ScrollToTop : Effect
    }
}
```

### Rules

| Component | Purpose | Characteristics |
|-----------|---------|-----------------|
| **UiState** | 화면 상태 | Immutable data class |
| **Event** | 사용자 액션 | Sealed interface |
| **Effect** | 일회성 효과 | Sealed interface |

## ViewModel Implementation

### File: `{Feature}ViewModel.kt`

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getLottoResultUseCase: GetLottoResultUseCase,
    private val getLatestRoundUseCase: GetLatestRoundUseCase
) : ViewModel() {

    // State
    private val _uiState = MutableStateFlow(HomeContract.UiState())
    val uiState: StateFlow<HomeContract.UiState> = _uiState.asStateFlow()

    // Effects (one-time events)
    private val _effect = Channel<HomeContract.Effect>()
    val effect = _effect.receiveAsFlow()

    // Event handler
    fun onEvent(event: HomeContract.Event) {
        when (event) {
            is HomeContract.Event.LoadLatest -> loadLatestResult()
            is HomeContract.Event.SelectRound -> selectRound(event.round)
            is HomeContract.Event.Search -> search(event.query)
            is HomeContract.Event.Refresh -> refresh()
        }
    }

    private fun loadLatestResult() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getLottoResultUseCase()
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(isLoading = false, lottoResults = listOf(result))
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                    _effect.send(HomeContract.Effect.ShowToast("Failed to load"))
                }
        }
    }

    private fun selectRound(round: Int) {
        _uiState.update { it.copy(selectedRound = round) }
    }

    private fun search(query: String) {
        // Implementation
    }

    private fun refresh() {
        loadLatestResult()
    }
}
```

## UI Integration

### Screen Composable

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is HomeContract.Effect.Navigate -> {
                    onNavigate(effect.route)
                }
                is HomeContract.Effect.ScrollToTop -> {
                    // Handle scroll
                }
            }
        }
    }

    HomeContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun HomeContent(
    uiState: HomeContract.UiState,
    onEvent: (HomeContract.Event) -> Unit
) {
    Column {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.error?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }

        LazyColumn {
            items(uiState.lottoResults) { result ->
                LottoResultItem(
                    result = result,
                    onClick = { onEvent(HomeContract.Event.SelectRound(result.round)) }
                )
            }
        }

        Button(onClick = { onEvent(HomeContract.Event.Refresh) }) {
            Text("Refresh")
        }
    }
}
```

## Best Practices

### DO
- UiState는 항상 immutable data class
- Event는 사용자 의도를 명확히 표현
- Effect는 일회성 이벤트만 (navigation, toast)
- StateFlow로 state, Channel로 effect

### DON'T
- UiState에 mutable 프로퍼티 사용
- Event에서 직접 로직 수행
- Effect를 state처럼 사용
- LiveData 사용 (StateFlow 사용)

## Testing

```kotlin
@Test
fun `loadLatest should update state with results`() = runTest {
    // Given
    val mockResult = LottoResult(round = 1, numbers = listOf(1,2,3,4,5,6))
    coEvery { getLottoResultUseCase() } returns Result.success(mockResult)

    // When
    viewModel.onEvent(HomeContract.Event.LoadLatest)

    // Then
    viewModel.uiState.test {
        val state = awaitItem()
        assertFalse(state.isLoading)
        assertEquals(1, state.lottoResults.size)
    }
}
```
