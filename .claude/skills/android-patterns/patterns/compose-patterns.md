# Compose Patterns Guide

Jetpack Compose 베스트 프랙티스 및 패턴 가이드입니다.

## State Hoisting

상태를 상위 컴포저블로 끌어올려 재사용성과 테스트 용이성을 높입니다.

### Bad Example
```kotlin
// ❌ 내부에서 상태 관리 - 테스트 어려움
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}
```

### Good Example
```kotlin
// ✅ 상태 호이스팅 - 재사용 가능, 테스트 용이
@Composable
fun Counter(
    count: Int,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onIncrement,
        modifier = modifier
    ) {
        Text("Count: $count")
    }
}

// Usage
@Composable
fun CounterScreen() {
    var count by remember { mutableStateOf(0) }
    Counter(
        count = count,
        onIncrement = { count++ }
    )
}
```

## Modifier Rules

### Parameter Order
```kotlin
// ✅ Modifier는 첫 번째 optional parameter
@Composable
fun CustomButton(
    text: String,                    // Required first
    modifier: Modifier = Modifier,   // Modifier second
    enabled: Boolean = true,         // Other optionals
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(text)
    }
}
```

### Modifier Chaining
```kotlin
// ✅ 논리적 순서로 체이닝
modifier = Modifier
    .fillMaxWidth()           // Layout first
    .padding(16.dp)           // Spacing
    .background(Color.White)  // Appearance
    .clickable { onClick() }  // Interaction last
```

### Conditional Modifier
```kotlin
// ✅ then() 사용
modifier = Modifier
    .fillMaxWidth()
    .then(
        if (isSelected) Modifier.border(2.dp, Color.Blue)
        else Modifier
    )
```

## Recomposition Optimization

### remember
```kotlin
// ✅ 비용이 큰 계산에 remember 사용
@Composable
fun ExpensiveList(items: List<Item>) {
    val sortedItems = remember(items) {
        items.sortedBy { it.name }  // items 변경 시에만 재계산
    }

    LazyColumn {
        items(sortedItems) { item ->
            ItemRow(item)
        }
    }
}
```

### derivedStateOf
```kotlin
// ✅ 파생 상태에 derivedStateOf 사용
@Composable
fun SearchableList(
    items: List<Item>,
    searchQuery: String
) {
    // searchQuery나 items가 변경될 때만 재계산
    val filteredItems by remember(items, searchQuery) {
        derivedStateOf {
            items.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    LazyColumn {
        items(filteredItems) { item ->
            ItemRow(item)
        }
    }
}
```

### key() for Lists
```kotlin
// ✅ LazyColumn에서 key 사용
LazyColumn {
    items(
        items = items,
        key = { item -> item.id }  // 안정적인 key로 recomposition 최적화
    ) { item ->
        ItemRow(item)
    }
}
```

## Screen/Content Pattern

### Screen: 상태 관리
```kotlin
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Effect 처리
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.NavigateBack -> onNavigateBack()
            }
        }
    }

    FeatureContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}
```

### Content: 순수 UI
```kotlin
@Composable
fun FeatureContent(
    uiState: UiState,
    onEvent: (Event) -> Unit,
    modifier: Modifier = Modifier
) {
    // 순수 UI 로직만 - ViewModel 참조 없음
    Column(modifier = modifier) {
        // UI components
    }
}
```

## Previews

```kotlin
@Preview(showBackground = true)
@Composable
private fun FeatureContentPreview() {
    MaterialTheme {
        FeatureContent(
            uiState = UiState(
                isLoading = false,
                items = listOf(
                    Item(id = 1, name = "Item 1"),
                    Item(id = 2, name = "Item 2")
                )
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun FeatureContentLoadingPreview() {
    MaterialTheme {
        FeatureContent(
            uiState = UiState(isLoading = true),
            onEvent = {}
        )
    }
}
```

## Common Patterns

### Loading State
```kotlin
@Composable
fun LoadingContent(isLoading: Boolean, content: @Composable () -> Unit) {
    Box {
        content()
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
```

### Error State
```kotlin
@Composable
fun ErrorContent(
    error: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    error?.let {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
```

### Empty State
```kotlin
@Composable
fun EmptyContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```
