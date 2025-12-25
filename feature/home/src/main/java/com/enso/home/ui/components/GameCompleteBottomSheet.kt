package com.enso.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.enso.designsystem.component.LottoPrimaryButtonFull
import com.enso.designsystem.component.LottoSecondaryButton
import com.enso.designsystem.theme.AppShapes
import com.enso.designsystem.theme.LocalLottoColors
import com.enso.home.R

/**
 * 게임 완료 바텀시트
 *
 * 6개 번호 선택이 완료되면 표시되는 바텀시트입니다.
 * 다른 게임 추가 또는 저장을 선택할 수 있습니다.
 *
 * @param gameName 완료된 게임 이름 (A, B, C, D, E)
 * @param numbers 선택된 번호 6개
 * @param sheetState 바텀시트 상태
 * @param canAddGame 게임 추가 가능 여부 (5개 미만일 때)
 * @param onAddGame 게임 추가 콜백
 * @param onSave 저장 콜백
 * @param onDismiss 닫기 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameCompleteBottomSheet(
    gameName: String,
    numbers: List<Int>,
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
            BottomSheetDragHandle()
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
                text = stringResource(R.string.manual_input_game_complete_title, gameName),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = lottoColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 부제목
            Text(
                text = stringResource(R.string.manual_input_game_complete_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = lottoColors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 선택된 번호 표시
            SelectedBallsRowForBottomSheet(
                selectedNumbers = numbers,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

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
 * 바텀시트 드래그 핸들
 */
@Composable
private fun BottomSheetDragHandle() {
    val lottoColors = LocalLottoColors.current

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
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                )
        )
    }
}
