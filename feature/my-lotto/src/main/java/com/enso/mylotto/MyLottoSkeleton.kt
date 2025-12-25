package com.enso.mylotto

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.enso.designsystem.modifier.ShimmerContainer
import com.enso.designsystem.modifier.shimmerEffect
import com.enso.designsystem.theme.LocalLottoColors
import com.enso.home.ui.components.TicketCardSkeleton

@Composable
fun MyLottoSkeletonContent(
    modifier: Modifier = Modifier
) {
    val lottoColors = LocalLottoColors.current

    // ShimmerContainer로 감싸서 모든 shimmerEffect()가 동일한 애니메이션을 공유
    ShimmerContainer {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = false
        ) {
            // 섹션 헤더 스켈레톤
            item(key = "header_skeleton") {
                HeaderSkeleton()
            }

            // 정렬 버튼 스켈레톤
            item(key = "sort_button_skeleton") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(lottoColors.backgroundLight)
                        .padding(bottom = 16.dp)
                ) {
                    SortButtonSkeleton()
                }
            }

            // 티켓 카드 스켈레톤 3개
            items(
                count = 3,
                key = { "ticket_skeleton_$it" }
            ) { index ->
                TicketCardSkeleton(
                    gameCount = when (index) {
                        0 -> 5
                        1 -> 3
                        else -> 4
                    }
                )
            }
        }
    }
}

@Composable
private fun HeaderSkeleton() {
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .shimmerEffect()
    )
}

@Composable
private fun SortButtonSkeleton() {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .shimmerEffect()
    )
}
