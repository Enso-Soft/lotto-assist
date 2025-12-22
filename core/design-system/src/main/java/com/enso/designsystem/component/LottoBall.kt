package com.enso.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.enso.designsystem.theme.LottoTheme
import com.enso.designsystem.theme.getLottoBallColor

/**
 * LottoBall - 로또 번호 공 컴포넌트
 *
 * 로또 번호를 표시하는 원형 공 컴포넌트입니다.
 * 번호 범위에 따라 자동으로 색상이 변경됩니다.
 *
 * @param number 로또 번호 (1-45)
 * @param modifier 외부에서 전달받는 Modifier
 * @param size 공의 크기 (기본값: 40dp)
 * @param textStyle 번호 텍스트 스타일 (기본값: labelLarge)
 */
@Composable
fun LottoBall(
    number: Int,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
) {
    val lottoColors = LottoTheme.colors
    val ballColor = getLottoBallColor(number, lottoColors)

    Box(
        modifier = modifier
            .size(size)
            .background(color = ballColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = textStyle,
            color = lottoColors.ballTextColor
        )
    }
}

/**
 * LottoBallTiny - 초소형 로또 공 (24dp)
 */
@Composable
fun LottoBallTiny(
    number: Int,
    modifier: Modifier = Modifier,
) {
    LottoBall(
        number = number,
        modifier = modifier,
        size = 24.dp,
        textStyle = LottoTheme.numberTypography.lottoNumberTiny
    )
}

/**
 * LottoBallSmall - 소형 로또 공 (32dp)
 */
@Composable
fun LottoBallSmall(
    number: Int,
    modifier: Modifier = Modifier,
) {
    LottoBall(
        number = number,
        modifier = modifier,
        size = 32.dp,
        textStyle = LottoTheme.numberTypography.lottoNumberSmall
    )
}

/**
 * LottoBallMedium - 중형 로또 공 (40dp) - 기본 크기
 */
@Composable
fun LottoBallMedium(
    number: Int,
    modifier: Modifier = Modifier,
) {
    LottoBall(
        number = number,
        modifier = modifier,
        size = 40.dp,
        textStyle = LottoTheme.numberTypography.lottoNumberMedium
    )
}

/**
 * LottoBallLarge - 대형 로또 공 (48dp)
 */
@Composable
fun LottoBallLarge(
    number: Int,
    modifier: Modifier = Modifier,
) {
    LottoBall(
        number = number,
        modifier = modifier,
        size = 48.dp,
        textStyle = LottoTheme.numberTypography.lottoNumberLarge
    )
}

// ========== Previews ==========

@Preview(name = "Default - Light", showBackground = true)
@Preview(name = "Default - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LottoBallPreview() {
    LottoTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            LottoBall(number = 1, modifier = Modifier.padding(4.dp))
            LottoBall(number = 15, modifier = Modifier.padding(4.dp))
            LottoBall(number = 25, modifier = Modifier.padding(4.dp))
            LottoBall(number = 35, modifier = Modifier.padding(4.dp))
            LottoBall(number = 45, modifier = Modifier.padding(4.dp))
        }
    }
}

@Preview(name = "All Balls (1-45) - Light", showBackground = true, widthDp = 720)
@Preview(name = "All Balls (1-45) - Dark", showBackground = true, widthDp = 720, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LottoBallAllNumbersPreview() {
    LottoTheme {
        LazyRow(modifier = Modifier.padding(16.dp)) {
            items((1..45).toList()) { number ->
                LottoBall(
                    number = number,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

@Preview(name = "Size Variants - Light", showBackground = true)
@Preview(name = "Size Variants - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LottoBallSizeVariantsPreview() {
    LottoTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LottoBallTiny(number = 7, modifier = Modifier.padding(4.dp))
            LottoBallSmall(number = 7, modifier = Modifier.padding(4.dp))
            LottoBallMedium(number = 7, modifier = Modifier.padding(4.dp))
            LottoBallLarge(number = 7, modifier = Modifier.padding(4.dp))
        }
    }
}

@Preview(name = "Portrait - Pixel 4", device = Devices.PIXEL_4, showBackground = true)
@Composable
private fun LottoBallPortraitPreview() {
    LottoTheme {
        LazyRow(modifier = Modifier.padding(16.dp)) {
            items((1..10).toList()) { number ->
                LottoBall(
                    number = number,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

@Preview(name = "Landscape", device = Devices.AUTOMOTIVE_1024p, widthDp = 720, heightDp = 360, showBackground = true)
@Composable
private fun LottoBallLandscapePreview() {
    LottoTheme {
        LazyRow(modifier = Modifier.padding(16.dp)) {
            items((1..20).toList()) { number ->
                LottoBall(
                    number = number,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

@Preview(name = "Small Screen", widthDp = 320, heightDp = 480, showBackground = true)
@Composable
private fun LottoBallSmallScreenPreview() {
    LottoTheme {
        LazyRow(modifier = Modifier.padding(8.dp)) {
            items((1..6).toList()) { number ->
                LottoBallSmall(
                    number = number,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

@Preview(name = "Large Text (1.5x)", fontScale = 1.5f, showBackground = true)
@Composable
private fun LottoBallLargeTextPreview() {
    LottoTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            LottoBall(number = 1, modifier = Modifier.padding(4.dp))
            LottoBall(number = 15, modifier = Modifier.padding(4.dp))
            LottoBall(number = 25, modifier = Modifier.padding(4.dp))
            LottoBall(number = 35, modifier = Modifier.padding(4.dp))
            LottoBall(number = 45, modifier = Modifier.padding(4.dp))
        }
    }
}
