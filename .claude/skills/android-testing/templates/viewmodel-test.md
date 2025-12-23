# ViewModel Test Template

ViewModel 테스트 작성을 위한 템플릿입니다.

## Basic Structure

```kotlin
@ExtendWith(MockKExtension::class)
class HomeViewModelTest {

    @MockK
    private lateinit var getLottoResultUseCase: GetLottoResultUseCase

    @MockK
    private lateinit var getLatestRoundUseCase: GetLatestRoundUseCase

    private lateinit var viewModel: HomeViewModel

    @BeforeEach
    fun setup() {
        viewModel = HomeViewModel(
            getLottoResultUseCase = getLottoResultUseCase,
            getLatestRoundUseCase = getLatestRoundUseCase
        )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
}
```

## State Testing with Turbine

### Loading State Test

```kotlin
@Test
fun `loadData should emit loading state first`() = runTest {
    // Given
    coEvery { getLottoResultUseCase(any()) } coAnswers {
        delay(100)
        Result.success(mockLottoResult)
    }

    viewModel.uiState.test {
        // Initial state
        assertEquals(HomeContract.UiState(), awaitItem())

        // When
        viewModel.onEvent(HomeContract.Event.LoadLatest)

        // Then - Loading state
        val loadingState = awaitItem()
        assertTrue(loadingState.isLoading)

        // Final state
        val finalState = awaitItem()
        assertFalse(finalState.isLoading)
        assertNotNull(finalState.lottoResults)

        cancelAndIgnoreRemainingEvents()
    }
}
```

### Success State Test

```kotlin
@Test
fun `loadData should update state with results when useCase succeeds`() = runTest {
    // Given
    val expectedResult = LottoResult(
        round = 1000,
        numbers = listOf(1, 2, 3, 4, 5, 6),
        bonusNumber = 7,
        drawDate = LocalDate.now(),
        prizeAmount = 1000000000L
    )
    coEvery { getLottoResultUseCase(any()) } returns Result.success(expectedResult)

    // When
    viewModel.onEvent(HomeContract.Event.LoadLatest)

    // Then
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.error)
    assertEquals(1, state.lottoResults.size)
    assertEquals(expectedResult, state.lottoResults.first())
}
```

### Error State Test

```kotlin
@Test
fun `loadData should update state with error when useCase fails`() = runTest {
    // Given
    val errorMessage = "Network error"
    coEvery { getLottoResultUseCase(any()) } returns Result.failure(Exception(errorMessage))

    // When
    viewModel.onEvent(HomeContract.Event.LoadLatest)

    // Then
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(errorMessage, state.error)
    assertTrue(state.lottoResults.isEmpty())
}
```

## Effect Testing

```kotlin
@Test
fun `should emit ShowToast effect when error occurs`() = runTest {
    // Given
    coEvery { getLottoResultUseCase(any()) } returns Result.failure(Exception("Error"))

    viewModel.effect.test {
        // When
        viewModel.onEvent(HomeContract.Event.LoadLatest)

        // Then
        val effect = awaitItem()
        assertTrue(effect is HomeContract.Effect.ShowToast)
        assertEquals("Error", (effect as HomeContract.Effect.ShowToast).message)

        cancelAndIgnoreRemainingEvents()
    }
}
```

## Event Handling Test

```kotlin
@Test
fun `onEvent SelectRound should update selectedRound in state`() = runTest {
    // Given
    val round = 1000

    // When
    viewModel.onEvent(HomeContract.Event.SelectRound(round))

    // Then
    assertEquals(round, viewModel.uiState.value.selectedRound)
}

@Test
fun `onEvent Refresh should reload data`() = runTest {
    // Given
    coEvery { getLottoResultUseCase(any()) } returns Result.success(mockResult)

    // When
    viewModel.onEvent(HomeContract.Event.Refresh)

    // Then
    advanceUntilIdle()
    coVerify(exactly = 1) { getLottoResultUseCase(any()) }
}
```

## Mock Data

```kotlin
companion object {
    private val mockLottoResult = LottoResult(
        round = 1000,
        numbers = listOf(1, 2, 3, 4, 5, 6),
        bonusNumber = 7,
        drawDate = LocalDate.of(2024, 1, 1),
        prizeAmount = 1000000000L
    )

    private val mockLottoResults = listOf(
        mockLottoResult,
        mockLottoResult.copy(round = 999),
        mockLottoResult.copy(round = 998)
    )
}
```

## Advanced Patterns

### Testing with SavedStateHandle

```kotlin
@Test
fun `should restore state from SavedStateHandle`() = runTest {
    // Given
    val savedStateHandle = SavedStateHandle(
        mapOf("selectedRound" to 1000)
    )

    viewModel = HomeViewModel(
        getLottoResultUseCase = getLottoResultUseCase,
        savedStateHandle = savedStateHandle
    )

    // Then
    assertEquals(1000, viewModel.uiState.value.selectedRound)
}
```

### Testing Multiple Events

```kotlin
@Test
fun `should handle multiple events in sequence`() = runTest {
    // Given
    coEvery { getLottoResultUseCase(any()) } returns Result.success(mockResult)

    viewModel.uiState.test {
        skipItems(1) // Skip initial state

        // When - Multiple events
        viewModel.onEvent(HomeContract.Event.LoadLatest)
        awaitItem() // Loading
        awaitItem() // Loaded

        viewModel.onEvent(HomeContract.Event.SelectRound(1000))
        val selectedState = awaitItem()

        // Then
        assertEquals(1000, selectedState.selectedRound)

        cancelAndIgnoreRemainingEvents()
    }
}
```
