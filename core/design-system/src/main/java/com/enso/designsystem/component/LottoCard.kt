package com.enso.designsystem.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.enso.designsystem.modifier.scaleOnPressEffect
import com.enso.designsystem.theme.AppShapes
import com.enso.designsystem.theme.LottoElevation

/**
 * Lotto Card
 * 약한 음영과 16dp 모서리, 밝은 표면 배경
 */
@Composable
fun LottoCard(
    modifier: Modifier = Modifier,
    shape: Shape = AppShapes.Card,
    containerColor: Color? = null,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor ?: MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = LottoElevation.low
        )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * Lotto Clickable Card
 * 터치 시 스케일 바운스 효과 적용
 */
@Composable
fun LottoClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = AppShapes.Card,
    containerColor: Color? = null,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        onClick = onClick,
        modifier = modifier.scaleOnPressEffect(interactionSource),
        shape = shape,
        interactionSource = interactionSource,
        colors = CardDefaults.cardColors(
            containerColor = containerColor ?: MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = LottoElevation.low,
            pressedElevation = LottoElevation.medium
        )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * Lotto Large Card
 * 24dp 모서리, 큰 패딩
 */
@Composable
fun LottoLargeCard(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentPadding: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    LottoCard(
        modifier = modifier,
        shape = AppShapes.CardLarge,
        containerColor = containerColor,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Lotto Section Card
 * 섹션 배경으로 사용, 패딩 없음
 */
@Composable
fun LottoSectionCard(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = containerColor ?: MaterialTheme.colorScheme.surface,
        shadowElevation = LottoElevation.low
    ) {
        Column(content = content)
    }
}

/**
 * Lotto Surface (배경색 차이로 계층 구분)
 */
@Composable
fun LottoSurface(
    modifier: Modifier = Modifier,
    level: SurfaceLevel = SurfaceLevel.Base,
    shape: Shape = AppShapes.Card,
    contentPadding: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val backgroundColor = when (level) {
        SurfaceLevel.Background -> MaterialTheme.colorScheme.background
        SurfaceLevel.Base -> MaterialTheme.colorScheme.surface
        SurfaceLevel.Elevated -> MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        shadowElevation = LottoElevation.none
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * Surface hierarchy levels (background color instead of elevation)
 */
enum class SurfaceLevel {
    Background,  // #F2F4F6 - 전체 배경
    Base,        // #FFFFFF - 카드, 기본 표면
    Elevated     // #F2F4F6 - 칩, 선택된 아이템 배경
}
