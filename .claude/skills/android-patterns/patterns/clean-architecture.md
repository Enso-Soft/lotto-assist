# Clean Architecture Guide

Clean Architecture 레이어별 구현 가이드입니다.

## Layer Dependencies

```
┌─────────────────────────────────────────────────────┐
│                  Presentation                       │
│    ┌─────────────────────────────────────────┐     │
│    │  ViewModel, Contract, Compose UI        │     │
│    └─────────────────────────────────────────┘     │
│                       │                             │
│                       ▼ uses                        │
├─────────────────────────────────────────────────────┤
│                    Domain                           │
│    ┌─────────────────────────────────────────┐     │
│    │  UseCase, Models, Repository Interface  │     │
│    └─────────────────────────────────────────┘     │
│                       ▲                             │
│                       │ implements                  │
├─────────────────────────────────────────────────────┤
│                     Data                            │
│    ┌─────────────────────────────────────────┐     │
│    │  Repository Impl, DataSource, DTO       │     │
│    └─────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────┘
```

## Domain Layer

### Characteristics
- **Pure Kotlin** - Android 의존성 없음
- **비즈니스 로직** - 핵심 규칙 포함
- **의존성 없음** - 가장 안쪽 레이어

### Models

```kotlin
// core/domain/src/.../model/LottoResult.kt
data class LottoResult(
    val round: Int,
    val numbers: List<Int>,
    val bonusNumber: Int,
    val drawDate: LocalDate,
    val prizeAmount: Long
)
```

### Repository Interface

```kotlin
// core/domain/src/.../repository/LottoRepository.kt
interface LottoRepository {
    suspend fun getLottoResult(round: Int): Result<LottoResult>
    suspend fun getLatestResult(): Result<LottoResult>
    fun getLottoResultsStream(): Flow<List<LottoResult>>
}
```

### UseCase

```kotlin
// core/domain/src/.../usecase/GetLottoResultUseCase.kt
class GetLottoResultUseCase @Inject constructor(
    private val repository: LottoRepository
) {
    suspend operator fun invoke(round: Int): Result<LottoResult> =
        repository.getLottoResult(round)
}

// UseCase with business logic
class AnalyzeLottoResultsUseCase @Inject constructor(
    private val repository: LottoRepository
) {
    suspend operator fun invoke(rounds: Int): Result<LottoAnalysis> {
        return repository.getLottoResults(rounds)
            .map { results ->
                LottoAnalysis(
                    mostFrequent = calculateMostFrequent(results),
                    leastFrequent = calculateLeastFrequent(results),
                    averageSum = calculateAverageSum(results)
                )
            }
    }

    private fun calculateMostFrequent(results: List<LottoResult>): List<Int> {
        // Business logic here
    }
}
```

## Data Layer

### Characteristics
- **Repository 구현** - Domain interface 구현
- **데이터 소스** - Remote/Local 분리
- **DTO/Entity** - 외부 데이터 구조

### Repository Implementation

```kotlin
// core/data/src/.../repository/LottoRepositoryImpl.kt
class LottoRepositoryImpl @Inject constructor(
    private val remoteDataSource: LottoRemoteDataSource,
    private val localDataSource: LottoLocalDataSource
) : LottoRepository {

    override suspend fun getLottoResult(round: Int): Result<LottoResult> =
        runCatching {
            // Try cache first
            localDataSource.getLottoResult(round)
                ?: remoteDataSource.fetchLottoResult(round)
                    .also { localDataSource.saveLottoResult(it) }
        }.map { it.toDomain() }

    override suspend fun getLatestResult(): Result<LottoResult> =
        runCatching {
            remoteDataSource.fetchLatestResult()
        }.map { it.toDomain() }

    override fun getLottoResultsStream(): Flow<List<LottoResult>> =
        localDataSource.observeLottoResults()
            .map { entities -> entities.map { it.toDomain() } }
}
```

### Remote DataSource

```kotlin
// core/network/src/.../datasource/LottoRemoteDataSource.kt
class LottoRemoteDataSource @Inject constructor(
    private val api: LottoApi
) {
    suspend fun fetchLottoResult(round: Int): LottoDto =
        api.getLottoResult(round)

    suspend fun fetchLatestResult(): LottoDto =
        api.getLatestResult()
}

// API Interface
interface LottoApi {
    @GET("lotto/{round}")
    suspend fun getLottoResult(@Path("round") round: Int): LottoDto

    @GET("lotto/latest")
    suspend fun getLatestResult(): LottoDto
}
```

### Local DataSource

```kotlin
// core/database/src/.../datasource/LottoLocalDataSource.kt
class LottoLocalDataSource @Inject constructor(
    private val lottoDao: LottoDao
) {
    suspend fun getLottoResult(round: Int): LottoEntity? =
        lottoDao.getByRound(round)

    suspend fun saveLottoResult(dto: LottoDto) {
        lottoDao.insert(dto.toEntity())
    }

    fun observeLottoResults(): Flow<List<LottoEntity>> =
        lottoDao.observeAll()
}

// Room DAO
@Dao
interface LottoDao {
    @Query("SELECT * FROM lotto_results WHERE round = :round")
    suspend fun getByRound(round: Int): LottoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LottoEntity)

    @Query("SELECT * FROM lotto_results ORDER BY round DESC")
    fun observeAll(): Flow<List<LottoEntity>>
}
```

### Mappers

```kotlin
// core/data/src/.../mapper/LottoMapper.kt

// DTO -> Domain
fun LottoDto.toDomain(): LottoResult = LottoResult(
    round = this.drwNo,
    numbers = listOf(drwtNo1, drwtNo2, drwtNo3, drwtNo4, drwtNo5, drwtNo6),
    bonusNumber = this.bnusNo,
    drawDate = LocalDate.parse(this.drwNoDate),
    prizeAmount = this.firstWinamnt
)

// Entity -> Domain
fun LottoEntity.toDomain(): LottoResult = LottoResult(
    round = this.round,
    numbers = this.numbers.split(",").map { it.toInt() },
    bonusNumber = this.bonusNumber,
    drawDate = this.drawDate,
    prizeAmount = this.prizeAmount
)

// DTO -> Entity
fun LottoDto.toEntity(): LottoEntity = LottoEntity(
    round = this.drwNo,
    numbers = listOf(drwtNo1, drwtNo2, drwtNo3, drwtNo4, drwtNo5, drwtNo6).joinToString(","),
    bonusNumber = this.bnusNo,
    drawDate = LocalDate.parse(this.drwNoDate),
    prizeAmount = this.firstWinamnt
)
```

## Presentation Layer

### Characteristics
- **ViewModel** - UI 로직, 상태 관리
- **Contract** - UiState, Event, Effect
- **Compose UI** - 화면 구성

### Structure

```kotlin
feature/home/
├── HomeScreen.kt       # Screen + Content Composables
├── HomeViewModel.kt    # ViewModel
├── HomeContract.kt     # UiState, Event, Effect
└── components/         # Reusable composables
    ├── LottoResultCard.kt
    └── NumberBall.kt
```

## Dependency Injection

### Hilt Modules

```kotlin
// core/di/src/.../RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindLottoRepository(
        impl: LottoRepositoryImpl
    ): LottoRepository
}

// core/di/src/.../NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideLottoApi(retrofit: Retrofit): LottoApi =
        retrofit.create(LottoApi::class.java)
}
```
