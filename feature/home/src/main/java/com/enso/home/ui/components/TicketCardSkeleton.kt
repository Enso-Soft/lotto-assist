package com.enso.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.enso.designsystem.modifier.shimmerEffect
import com.enso.designsystem.theme.LocalLottoColors

@Composable
fun TicketCardSkeleton(
    modifier: Modifier = Modifier,
    gameCount: Int = 3
) {
    val lottoColors = LocalLottoColors.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = lottoColors.cardLight),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 티켓 헤더 스켈레톤: 회차 + 등록일
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 회차 스켈레톤
                    SkeletonBox(
                        width = 60,
                        height = 20
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // 등록일 스켈레톤
                    SkeletonBox(
                        width = 80,
                        height = 16
                    )
                }
                // 삭제 버튼 스켈레톤
                SkeletonBox(
                    width = 24,
                    height = 24,
                    shape = CircleShape
                )
            }

            // 게임 목록 스켈레톤
            repeat(gameCount) {
                GameRowSkeleton()
            }
        }
    }
}

@Composable
private fun GameRowSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 게임 레이블 (A, B, C, D, E)
        SkeletonBox(
            width = 16,
            height = 16
        )

        // 게임 타입 (자동/수동)
        SkeletonBox(
            width = 25,
            height = 16
        )

        Spacer(modifier = Modifier.weight(1f))

        // 로또 번호 6개
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(6) {
                LottoBallSkeleton()
            }
        }
    }
}

@Composable
private fun LottoBallSkeleton() {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .shimmerEffect()
    )
}

@Composable
private fun SkeletonBox(
    width: Int,
    height: Int,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(4.dp)
) {
    Box(
        modifier = Modifier
            .width(width.dp)
            .height(height.dp)
            .clip(shape)
            .shimmerEffect()
    )
}
