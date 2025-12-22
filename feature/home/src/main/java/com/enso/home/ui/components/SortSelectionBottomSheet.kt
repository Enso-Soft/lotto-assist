package com.enso.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enso.designsystem.theme.LocalLottoColors
import com.enso.domain.model.TicketSortType
import com.enso.home.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSelectionBottomSheet(
    currentSortType: TicketSortType,
    onSelectSortType: (TicketSortType) -> Unit,
    onDismiss: () -> Unit
) {
    val lottoColors = LocalLottoColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = lottoColors.cardLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                stringResource(R.string.home_sort_criteria),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = lottoColors.textMainLight,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TicketSortType.entries.forEach { sortType ->
                Card(
                    onClick = { onSelectSortType(sortType) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (sortType == currentSortType) {
                            lottoColors.primary.copy(alpha = 0.1f)
                        } else {
                            lottoColors.cardLight
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            sortType.displayName,
                            fontWeight = if (sortType == currentSortType) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp,
                            color = if (sortType == currentSortType) lottoColors.primary else lottoColors.textMainLight
                        )
                        if (sortType == currentSortType) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = stringResource(R.string.home_selected),
                                tint = lottoColors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
