package com.enso.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enso.designsystem.theme.LottoElevation
import com.enso.designsystem.theme.LottoTheme

/**
 * LottoCard - Material3 Card wrapper
 *
 * 일관된 스타일을 가진 카드 컴포넌트입니다.
 *
 * @param modifier 외부에서 전달받는 Modifier
 * @param shape 카드의 모서리 형태 (기본값: medium)
 * @param colors 카드 색상 설정
 * @param elevation 카드 그림자 높이
 * @param onClick 클릭 이벤트 핸들러 (nullable)
 * @param content 카드 내부 컨텐츠
 */
@Composable
fun LottoCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(
        defaultElevation = LottoElevation.medium
    ),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            content = content
        )
    }
}

// ========== Previews ==========

@Preview(name = "Default - Light", showBackground = true)
@Preview(name = "Default - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LottoCardPreview() {
    LottoTheme {
        LottoCard(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(LottoTheme.spacing.cardPadding)) {
                Text(
                    text = "로또 카드",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "이것은 로또 카드의 내용입니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(name = "Clickable Card - Light", showBackground = true)
@Preview(name = "Clickable Card - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LottoCardClickablePreview() {
    LottoTheme {
        LottoCard(
            modifier = Modifier.padding(16.dp),
            onClick = { /* Click handler */ }
        ) {
            Column(modifier = Modifier.padding(LottoTheme.spacing.cardPadding)) {
                Text(
                    text = "클릭 가능한 카드",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "이 카드는 클릭할 수 있습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(name = "Portrait - Pixel 4", device = Devices.PIXEL_4, showBackground = true)
@Composable
private fun LottoCardPortraitPreview() {
    LottoTheme {
        LottoCard(
            modifier = Modifier.padding(LottoTheme.spacing.md)
        ) {
            Column(modifier = Modifier.padding(LottoTheme.spacing.cardPadding)) {
                Text(
                    text = "세로 모드 카드",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "세로 화면에서 카드가 어떻게 보이는지 확인합니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(name = "Landscape", device = Devices.AUTOMOTIVE_1024p, widthDp = 720, heightDp = 360, showBackground = true)
@Composable
private fun LottoCardLandscapePreview() {
    LottoTheme {
        LottoCard(
            modifier = Modifier.padding(LottoTheme.spacing.md)
        ) {
            Column(modifier = Modifier.padding(LottoTheme.spacing.cardPadding)) {
                Text(
                    text = "가로 모드 카드",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "가로 화면에서 카드가 어떻게 보이는지 확인합니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(name = "Small Screen", widthDp = 320, heightDp = 480, showBackground = true)
@Composable
private fun LottoCardSmallScreenPreview() {
    LottoTheme {
        LottoCard(
            modifier = Modifier.padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(LottoTheme.spacing.sm)) {
                Text(
                    text = "작은 화면",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "작은 화면에서의 카드입니다.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(name = "Large Text (1.5x)", fontScale = 1.5f, showBackground = true)
@Composable
private fun LottoCardLargeTextPreview() {
    LottoTheme {
        LottoCard(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(LottoTheme.spacing.cardPadding)) {
                Text(
                    text = "큰 텍스트",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "폰트 크기가 1.5배 증가한 카드입니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
