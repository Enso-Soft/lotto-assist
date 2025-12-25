package com.enso.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.enso.designsystem.component.LottoPrimaryButtonFull
import com.enso.designsystem.component.LottoSecondaryButton
import com.enso.designsystem.theme.AppShapes
import com.enso.designsystem.theme.LocalLottoColors
import com.enso.designsystem.theme.getLottoBallColor
import com.enso.home.ManualGame
import com.enso.home.R

/**
 * 저장 확인 바텀시트
 *
 * 저장하기 버튼을 누르면 표시되는 바텀시트입니다.
 * 완료된 게임들을 표시하고, 다른 게임 추가 또는 저장을 선택할 수 있습니다.
 *
 * @param completedGames 완료된 게임 목록
 * @param sheetState 바텀시트 상태
 * @param canAddGame 게임 추가 가능 여부 (5개 미만일 때)
 * @param onAddGame 게임 추가 콜백
 * @param onSave 저장 콜백
 * @param onDismiss 닫기 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveConfirmBottomSheet(
    completedGames: List<ManualGame>,
    sheetState: SheetState,
    canAddGame: Boolean,
    onAddGame: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val lottoColors = LocalLottoColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = AppShapes.BottomSheet,
        containerColor = lottoColors.cardLight,
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth(0.1f)
                        .background(
                            color = lottoColors.divider,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 제목
            Text(
                text = "게임 저장",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = lottoColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 부제목
            Text(
                text = "${completedGames.size}게임을 저장할까요?",
                style = MaterialTheme.typography.bodyMedium,
                color = lottoColors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 완료된 게임들 표시
            completedGames.forEach { game ->
                GameNumbersRow(
                    gameName = game.id,
                    numbers = game.numbers.sorted(),
                    isAuto = game.isAuto
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 버튼들
            if (canAddGame) {
                LottoSecondaryButton(
                    text = stringResource(R.string.manual_input_add_another_game),
                    onClick = onAddGame,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            LottoPrimaryButtonFull(
                text = stringResource(R.string.manual_input_save),
                onClick = onSave
            )
        }
    }
}

/**
 * 게임 번호 Row
 */
@Composable
private fun GameNumbersRow(
    gameName: String,
    numbers: List<Int>,
    isAuto: Boolean
) {
    val lottoColors = LocalLottoColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = lottoColors.chipBackground,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        // 게임 이름 + 자동/수동
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "게임 $gameName",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = lottoColors.textPrimary
            )
            Text(
                text = if (isAuto) "자동" else "수동",
                style = MaterialTheme.typography.labelSmall,
                color = lottoColors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 번호들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            numbers.forEach { number ->
                SmallBall(number = number)
            }
        }
    }
}

/**
 * 작은 공 (바텀시트용)
 */
@Composable
private fun SmallBall(number: Int) {
    val lottoColors = LocalLottoColors.current
    val ballColor = getLottoBallColor(number, lottoColors)
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(ballColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = with(density) { 12.dp.toSp() }
            ),
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
