package com.enso.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enso.designsystem.theme.LottoTheme

/**
 * LottoButton - Primary Button
 *
 * 주요 액션을 위한 버튼 컴포넌트입니다.
 *
 * @param onClick 클릭 이벤트 핸들러
 * @param modifier 외부에서 전달받는 Modifier
 * @param enabled 버튼 활성화 상태 (기본값: true)
 * @param shape 버튼 모서리 형태
 * @param colors 버튼 색상 설정
 * @param elevation 버튼 그림자 높이
 * @param contentPadding 버튼 내부 패딩
 * @param content 버튼 내부 컨텐츠
 */
@Composable
fun LottoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.small,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * LottoOutlinedButton - Outlined Button
 *
 * 보조 액션을 위한 테두리 버튼 컴포넌트입니다.
 *
 * @param onClick 클릭 이벤트 핸들러
 * @param modifier 외부에서 전달받는 Modifier
 * @param enabled 버튼 활성화 상태 (기본값: true)
 * @param shape 버튼 모서리 형태
 * @param colors 버튼 색상 설정
 * @param elevation 버튼 그림자 높이
 * @param contentPadding 버튼 내부 패딩
 * @param content 버튼 내부 컨텐츠
 */
@Composable
fun LottoOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.small,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * LottoTextButton - Text Button
 *
 * 최소 강조 액션을 위한 텍스트 버튼 컴포넌트입니다.
 *
 * @param onClick 클릭 이벤트 핸들러
 * @param modifier 외부에서 전달받는 Modifier
 * @param enabled 버튼 활성화 상태 (기본값: true)
 * @param shape 버튼 모서리 형태
 * @param colors 버튼 색상 설정
 * @param contentPadding 버튼 내부 패딩
 * @param content 버튼 내부 컨텐츠
 */
@Composable
fun LottoTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.small,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * LottoTonalButton - Filled Tonal Button
 *
 * 보조 강조 액션을 위한 Tonal 버튼 컴포넌트입니다.
 *
 * @param onClick 클릭 이벤트 핸들러
 * @param modifier 외부에서 전달받는 Modifier
 * @param enabled 버튼 활성화 상태 (기본값: true)
 * @param shape 버튼 모서리 형태
 * @param colors 버튼 색상 설정
 * @param elevation 버튼 그림자 높이
 * @param contentPadding 버튼 내부 패딩
 * @param content 버튼 내부 컨텐츠
 */
@Composable
fun LottoTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.small,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.filledTonalButtonElevation(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        contentPadding = contentPadding,
        content = content
    )
}

// ========== Previews ==========

@Preview(name = "Button Variants - Light", showBackground = true)
@Preview(name = "Button Variants - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LottoButtonVariantsPreview() {
    LottoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LottoButton(
                onClick = { },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("Primary Button")
            }

            LottoTonalButton(
                onClick = { },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("Tonal Button")
            }

            LottoOutlinedButton(
                onClick = { },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("Outlined Button")
            }

            LottoTextButton(
                onClick = { },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("Text Button")
            }
        }
    }
}

@Preview(name = "Disabled States - Light", showBackground = true)
@Preview(name = "Disabled States - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LottoButtonDisabledPreview() {
    LottoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LottoButton(
                onClick = { },
                enabled = false,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("Disabled Primary")
            }

            LottoOutlinedButton(
                onClick = { },
                enabled = false,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("Disabled Outlined")
            }
        }
    }
}

@Preview(name = "Portrait - Pixel 4", device = Devices.PIXEL_4, showBackground = true)
@Composable
private fun LottoButtonPortraitPreview() {
    LottoTheme {
        Column(modifier = Modifier.padding(LottoTheme.spacing.md)) {
            LottoButton(
                onClick = { },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("세로 모드 버튼")
            }
        }
    }
}

@Preview(name = "Landscape", device = Devices.AUTOMOTIVE_1024p, widthDp = 720, heightDp = 360, showBackground = true)
@Composable
private fun LottoButtonLandscapePreview() {
    LottoTheme {
        Column(modifier = Modifier.padding(LottoTheme.spacing.md)) {
            LottoButton(
                onClick = { },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("가로 모드 버튼")
            }
        }
    }
}

@Preview(name = "Small Screen", widthDp = 320, heightDp = 480, showBackground = true)
@Composable
private fun LottoButtonSmallScreenPreview() {
    LottoTheme {
        Column(modifier = Modifier.padding(8.dp)) {
            LottoButton(
                onClick = { },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("작은 화면")
            }

            LottoOutlinedButton(
                onClick = { },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("작은 화면")
            }
        }
    }
}

@Preview(name = "Large Text (1.5x)", fontScale = 1.5f, showBackground = true)
@Composable
private fun LottoButtonLargeTextPreview() {
    LottoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LottoButton(
                onClick = { },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("큰 텍스트 버튼")
            }

            LottoOutlinedButton(
                onClick = { },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("큰 텍스트 버튼")
            }
        }
    }
}
