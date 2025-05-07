package com.example.pbl5.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FishStats(
    totalFishCount: Int,
    deadFishCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color.White,
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Total fish count
            StatItem(
                count = totalFishCount,
                label = "Số cá trong hồ",
                iconBackground = Color(0xFF1E90FF).copy(alpha = 0.1f),
                iconTint = Color(0xFF1E90FF),
                textColor = Color.Black,
                icon = Icons.Outlined.Visibility
            )

            // Vertical divider
            Divider(
                modifier = Modifier
                    .height(60.dp)
                    .width(1.dp),
                color = Color.LightGray
            )

            // Dead fish count
            StatItem(
                count = deadFishCount,
                label = "Số cá chết",
                iconBackground = Color.Red.copy(alpha = 0.1f),
                iconTint = Color.Red,
                textColor = Color.Red,
                icon = Icons.Default.Warning
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun StatItem(
    count: Int,
    label: String,
    iconBackground: Color,
    iconTint: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with background
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBackground)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Label
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Animated count
        AnimatedContent(
            targetState = count,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) with
                        fadeOut(animationSpec = tween(300))
            }
        ) { targetCount ->
            Text(
                text = targetCount.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}