package com.enso.home.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enso.ui.R
import com.enso.home.ui.theme.CardLight
import com.enso.home.ui.theme.Primary
import com.enso.home.ui.theme.TextMainLight
import com.enso.home.ui.theme.TextSubLight

/**
 * 토스 스타일 삭제 확인 다이얼로그
 * - 둥근 모서리 (24dp)
 * - 깔끔한 타이포그래피
 * - Primary 색상 강조
 */
@Composable
fun ConfirmDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dialog_delete_ticket_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextMainLight
            )
        },
        text = {
            Text(
                text = stringResource(R.string.dialog_delete_ticket_message),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 15.sp,
                color = TextSubLight,
                modifier = Modifier.padding(top = 8.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.dialog_delete_ticket_confirm),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = onDismiss,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = TextSubLight.copy(alpha = 0.1f),
                    contentColor = TextSubLight
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.dialog_delete_ticket_cancel),
                    fontSize = 15.sp
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = CardLight,
        tonalElevation = 6.dp
    )
}
