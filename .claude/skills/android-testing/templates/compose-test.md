# Compose UI Test Template

Compose UI 테스트 작성을 위한 템플릿입니다.

## Basic Setup

```kotlin
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `should display loading indicator when loading`() {
        // Given
        val uiState = HomeContract.UiState(isLoading = true)

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HomeContent(
                    uiState = uiState,
                    onEvent = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("loading_indicator")
            .assertIsDisplayed()
    }
}
```

## Finding Nodes

### By Test Tag

```kotlin
// In Composable
CircularProgressIndicator(
    modifier = Modifier.testTag("loading_indicator")
)

// In Test
composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
```

### By Text

```kotlin
composeTestRule.onNodeWithText("Submit").assertIsDisplayed()
composeTestRule.onNodeWithText("Error", substring = true).assertExists()
```

### By Content Description

```kotlin
// In Composable
Icon(
    imageVector = Icons.Default.Refresh,
    contentDescription = "Refresh button"
)

// In Test
composeTestRule.onNodeWithContentDescription("Refresh button").performClick()
```

### By Role

```kotlin
composeTestRule.onNode(hasRole(Role.Button)).performClick()
composeTestRule.onAllNodes(hasRole(Role.Button)).assertCountEquals(3)
```

## Assertions

```kotlin
// Visibility
composeTestRule.onNodeWithTag("item").assertIsDisplayed()
composeTestRule.onNodeWithTag("item").assertIsNotDisplayed()
composeTestRule.onNodeWithTag("item").assertExists()
composeTestRule.onNodeWithTag("item").assertDoesNotExist()

// Enabled state
composeTestRule.onNodeWithTag("button").assertIsEnabled()
composeTestRule.onNodeWithTag("button").assertIsNotEnabled()

// Selection state
composeTestRule.onNodeWithTag("checkbox").assertIsSelected()
composeTestRule.onNodeWithTag("checkbox").assertIsNotSelected()

// Text content
composeTestRule.onNodeWithTag("text").assertTextEquals("Expected Text")
composeTestRule.onNodeWithTag("text").assertTextContains("partial")
```

## User Interactions

```kotlin
// Click
composeTestRule.onNodeWithTag("button").performClick()

// Text input
composeTestRule.onNodeWithTag("text_field").performTextInput("Hello")
composeTestRule.onNodeWithTag("text_field").performTextClearance()
composeTestRule.onNodeWithTag("text_field").performTextReplacement("New text")

// Scroll
composeTestRule.onNodeWithTag("list").performScrollToIndex(5)
composeTestRule.onNodeWithTag("list").performScrollToNode(hasText("Item 10"))

// Swipe
composeTestRule.onNodeWithTag("item").performTouchInput {
    swipeLeft()
    swipeRight()
    swipeUp()
    swipeDown()
}
```

## Testing State Changes

```kotlin
@Test
fun `should show results when data loaded`() {
    // Given
    val initialState = HomeContract.UiState(isLoading = true)
    var currentState by mutableStateOf(initialState)

    composeTestRule.setContent {
        MaterialTheme {
            HomeContent(
                uiState = currentState,
                onEvent = {}
            )
        }
    }

    // Initial - Loading
    composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()

    // When - State changes
    currentState = HomeContract.UiState(
        isLoading = false,
        lottoResults = listOf(mockLottoResult)
    )

    // Then - Results shown
    composeTestRule.onNodeWithTag("loading_indicator").assertDoesNotExist()
    composeTestRule.onNodeWithTag("results_list").assertIsDisplayed()
}
```

## Testing Events

```kotlin
@Test
fun `should call onEvent when button clicked`() {
    // Given
    var capturedEvent: HomeContract.Event? = null

    composeTestRule.setContent {
        MaterialTheme {
            HomeContent(
                uiState = HomeContract.UiState(),
                onEvent = { event -> capturedEvent = event }
            )
        }
    }

    // When
    composeTestRule.onNodeWithTag("refresh_button").performClick()

    // Then
    assertEquals(HomeContract.Event.Refresh, capturedEvent)
}
```

## Testing Lists

```kotlin
@Test
fun `should display all items in list`() {
    // Given
    val items = listOf(
        LottoResult(round = 1, ...),
        LottoResult(round = 2, ...),
        LottoResult(round = 3, ...)
    )
    val uiState = HomeContract.UiState(lottoResults = items)

    composeTestRule.setContent {
        MaterialTheme {
            HomeContent(uiState = uiState, onEvent = {})
        }
    }

    // Then
    composeTestRule.onAllNodesWithTag("lotto_item").assertCountEquals(3)
}

@Test
fun `should call onEvent with correct item when item clicked`() {
    // Given
    val items = listOf(
        LottoResult(round = 1000, ...),
        LottoResult(round = 999, ...)
    )
    var selectedRound: Int? = null

    composeTestRule.setContent {
        MaterialTheme {
            HomeContent(
                uiState = HomeContract.UiState(lottoResults = items),
                onEvent = { event ->
                    if (event is HomeContract.Event.SelectRound) {
                        selectedRound = event.round
                    }
                }
            )
        }
    }

    // When - Click first item
    composeTestRule.onAllNodesWithTag("lotto_item")[0].performClick()

    // Then
    assertEquals(1000, selectedRound)
}
```

## Testing with Hilt

```kotlin
@HiltAndroidTest
class HomeScreenIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var repository: LottoRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `should display data from repository`() {
        // Given - Repository returns data
        // ...

        // Then
        composeTestRule.onNodeWithTag("results_list").assertIsDisplayed()
    }
}
```

## Common Test Tags

```kotlin
object TestTags {
    const val LOADING_INDICATOR = "loading_indicator"
    const val ERROR_MESSAGE = "error_message"
    const val EMPTY_STATE = "empty_state"
    const val RESULTS_LIST = "results_list"
    const val LOTTO_ITEM = "lotto_item"
    const val REFRESH_BUTTON = "refresh_button"
    const val SEARCH_FIELD = "search_field"
}
```
