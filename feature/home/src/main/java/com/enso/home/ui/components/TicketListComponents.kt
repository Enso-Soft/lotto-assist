package com.enso.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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

@Composable
fun AllTicketsHeader(
    totalCount: Int,
    winningCount: Int
) {
    val lottoColors = LocalLottoColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = lottoColors.primary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$totalCount",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = lottoColors.primary
                )
                Text(
                    text = stringResource(R.string.home_total_numbers),
                    fontSize = 12.sp,
                    color = lottoColors.textSubLight,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(50.dp)
                    .background(lottoColors.textSubLight.copy(alpha = 0.2f))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$winningCount",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = lottoColors.winningGreen
                )
                Text(
                    text = stringResource(R.string.home_winning_count),
                    fontSize = 12.sp,
                    color = lottoColors.textSubLight,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SortButton(
    currentSortType: TicketSortType,
    onClick: () -> Unit
) {
    val lottoColors = LocalLottoColors.current

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = lottoColors.cardLight),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_sort),
                    fontSize = 14.sp,
                    color = lottoColors.textSubLight
                )
                Text(
                    text = currentSortType.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = lottoColors.primary
                )
            }
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = stringResource(R.string.home_sort_select),
                tint = lottoColors.textSubLight,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
