# UseCase Test Template

UseCase 테스트 작성을 위한 템플릿입니다.

## Basic Structure

```kotlin
@ExtendWith(MockKExtension::class)
class GetLottoResultUseCaseTest {

    @MockK
    private lateinit var repository: LottoRepository

    private lateinit var useCase: GetLottoResultUseCase

    @BeforeEach
    fun setup() {
        useCase = GetLottoResultUseCase(repository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
}
```

## Simple UseCase Tests

### Success Case

```kotlin
@Test
fun `invoke should return result when repository succeeds`() = runTest {
    // Given
    val round = 1000
    val expectedResult = LottoResult(
        round = round,
        numbers = listOf(1, 2, 3, 4, 5, 6),
        bonusNumber = 7,
        drawDate = LocalDate.now(),
        prizeAmount = 1000000000L
    )
    coEvery { repository.getLottoResult(round) } returns Result.success(expectedResult)

    // When
    val result = useCase(round)

    // Then
    assertTrue(result.isSuccess)
    assertEquals(expectedResult, result.getOrNull())
    coVerify(exactly = 1) { repository.getLottoResult(round) }
}
```

### Failure Case

```kotlin
@Test
fun `invoke should return failure when repository fails`() = runTest {
    // Given
    val round = 1000
    val exception = Exception("Network error")
    coEvery { repository.getLottoResult(round) } returns Result.failure(exception)

    // When
    val result = useCase(round)

    // Then
    assertTrue(result.isFailure)
    assertEquals("Network error", result.exceptionOrNull()?.message)
}
```

## UseCase with Business Logic

```kotlin
class AnalyzeLottoResultsUseCaseTest {

    @MockK
    private lateinit var repository: LottoRepository

    private lateinit var useCase: AnalyzeLottoResultsUseCase

    @BeforeEach
    fun setup() {
        useCase = AnalyzeLottoResultsUseCase(repository)
    }

    @Test
    fun `invoke should calculate most frequent numbers correctly`() = runTest {
        // Given
        val mockResults = listOf(
            createLottoResult(numbers = listOf(1, 2, 3, 4, 5, 6)),
            createLottoResult(numbers = listOf(1, 2, 3, 7, 8, 9)),
            createLottoResult(numbers = listOf(1, 2, 10, 11, 12, 13))
        )
        coEvery { repository.getLottoResults(any()) } returns Result.success(mockResults)

        // When
        val result = useCase(3)

        // Then
        assertTrue(result.isSuccess)
        val analysis = result.getOrNull()!!

        // 1, 2는 3번, 3은 2번 등장
        assertTrue(analysis.mostFrequent.contains(1))
        assertTrue(analysis.mostFrequent.contains(2))
    }

    @Test
    fun `invoke should calculate average sum correctly`() = runTest {
        // Given
        val mockResults = listOf(
            createLottoResult(numbers = listOf(1, 2, 3, 4, 5, 6)),    // sum = 21
            createLottoResult(numbers = listOf(10, 20, 30, 40, 41, 42)) // sum = 183
        )
        coEvery { repository.getLottoResults(any()) } returns Result.success(mockResults)

        // When
        val result = useCase(2)

        // Then
        assertTrue(result.isSuccess)
        val analysis = result.getOrNull()!!
        assertEquals(102.0, analysis.averageSum, 0.01) // (21 + 183) / 2
    }

    private fun createLottoResult(
        round: Int = 1000,
        numbers: List<Int>
    ) = LottoResult(
        round = round,
        numbers = numbers,
        bonusNumber = 45,
        drawDate = LocalDate.now(),
        prizeAmount = 1000000000L
    )
}
```

## UseCase with Multiple Dependencies

```kotlin
class SyncLottoDataUseCaseTest {

    @MockK
    private lateinit var remoteRepository: LottoRemoteRepository

    @MockK
    private lateinit var localRepository: LottoLocalRepository

    @MockK
    private lateinit var networkChecker: NetworkChecker

    private lateinit var useCase: SyncLottoDataUseCase

    @BeforeEach
    fun setup() {
        useCase = SyncLottoDataUseCase(
            remoteRepository = remoteRepository,
            localRepository = localRepository,
            networkChecker = networkChecker
        )
    }

    @Test
    fun `invoke should sync data when network available`() = runTest {
        // Given
        val remoteData = listOf(mockLottoResult)
        every { networkChecker.isConnected() } returns true
        coEvery { remoteRepository.fetchAll() } returns Result.success(remoteData)
        coEvery { localRepository.saveAll(any()) } returns Unit

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        coVerify { remoteRepository.fetchAll() }
        coVerify { localRepository.saveAll(remoteData) }
    }

    @Test
    fun `invoke should return local data when network unavailable`() = runTest {
        // Given
        val localData = listOf(mockLottoResult)
        every { networkChecker.isConnected() } returns false
        coEvery { localRepository.getAll() } returns Result.success(localData)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(localData, result.getOrNull())
        coVerify(exactly = 0) { remoteRepository.fetchAll() }
    }
}
```

## Testing Flow-Returning UseCase

```kotlin
class ObserveLottoResultsUseCaseTest {

    @MockK
    private lateinit var repository: LottoRepository

    private lateinit var useCase: ObserveLottoResultsUseCase

    @BeforeEach
    fun setup() {
        useCase = ObserveLottoResultsUseCase(repository)
    }

    @Test
    fun `invoke should emit results from repository flow`() = runTest {
        // Given
        val results1 = listOf(mockLottoResult.copy(round = 1))
        val results2 = listOf(mockLottoResult.copy(round = 1), mockLottoResult.copy(round = 2))

        every { repository.observeResults() } returns flow {
            emit(results1)
            emit(results2)
        }

        // When & Then
        useCase().test {
            assertEquals(results1, awaitItem())
            assertEquals(results2, awaitItem())
            awaitComplete()
        }
    }
}
```

## Edge Cases

```kotlin
@Test
fun `invoke should handle empty result`() = runTest {
    // Given
    coEvery { repository.getLottoResults(any()) } returns Result.success(emptyList())

    // When
    val result = useCase(10)

    // Then
    assertTrue(result.isSuccess)
    assertTrue(result.getOrNull()?.isEmpty() == true)
}

@Test
fun `invoke should handle invalid input`() = runTest {
    // When
    val result = useCase(-1) // Invalid round number

    // Then
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is IllegalArgumentException)
}
```
