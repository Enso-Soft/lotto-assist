package com.enso.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.enso.home.R
import androidx.compose.ui.unit.sp
import com.enso.home.ui.theme.Primary
import com.enso.home.ui.theme.getLottoBallColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManualInputDialog(
    currentRound: Int,
    onDismiss: () -> Unit,
    onConfirm: (round: Int, numbers: List<Int>, isAuto: Boolean) -> Unit
) {
    val selectedNumbers = remember { mutableStateListOf<Int>() }
    var isAuto by remember { mutableStateOf(false) }
    var round by remember { mutableStateOf(currentRound) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.dialog_manual_input_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 자동/수동 선택
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.dialog_auto_select))
                    Switch(
                        checked = isAuto,
                        onCheckedChange = { isAuto = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 선택된 번호 표시
                Text(
                    stringResource(R.string.dialog_selected_numbers_format, selectedNumbers.size),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedNumbers.isEmpty()) {
                    Text(
                        stringResource(R.string.dialog_select_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        selectedNumbers.sorted().forEach { number ->
                            NumberBall(
                                number = number,
                                isSelected = true,
                                onClick = { selectedNumbers.remove(number) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 번호 선택 그리드
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..45).forEach { number ->
                        NumberBall(
                            number = number,
                            isSelected = selectedNumbers.contains(number),
                            onClick = {
                                if (selectedNumbers.contains(number)) {
                                    selectedNumbers.remove(number)
                                } else if (selectedNumbers.size < 6) {
                                    selectedNumbers.add(number)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { selectedNumbers.clear() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.dialog_reset))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedNumbers.size == 6) {
                        onConfirm(round, selectedNumbers.toList(), isAuto)
                        onDismiss()
                    }
                },
                enabled = selectedNumbers.size == 6,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                )
            ) {
                Text(stringResource(R.string.dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@Composable
private fun NumberBall(
    number: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        getLottoBallColor(number)
    } else {
        Color.LightGray.copy(alpha = 0.3f)
    }

    val textColor = if (isSelected) {
        Color.White
    } else {
        Color.Gray
    }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}
