package com.example.pbl5.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pbl5.data.NotificationData
import com.example.pbl5.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    notifications: List<NotificationData>,
    viewModel: MainViewModel,
    onNotificationsUpdated: () -> Unit
) {
    var showNotificationDialog by remember { mutableStateOf(false) }
    val unreadCount = notifications.count { !it.isRead }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E90FF).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (title.isNotEmpty()) {
                        Text(
                            text = title.first().toString(),
                            color = Color(0xFF1E90FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "User",
                            tint = Color(0xFF1E90FF),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
            }
        },
        actions = {
            // Tách thành composable riêng để tránh RowScope
            NotificationIconWithBadge(
                unreadCount = unreadCount,
                onClick = {
                    showNotificationDialog = true
                    onNotificationsUpdated()
                }
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        )
    )

    if (showNotificationDialog) {
        NotificationDialog(
            notifications = notifications,
            onDismiss = { showNotificationDialog = false },
            onMarkAsRead = { notificationId ->
                viewModel.markNotificationAsRead(notificationId)
            }
        )
    }
}

@Composable
fun NotificationIconWithBadge(
    unreadCount: Int,
    onClick: () -> Unit
) {
    Box {
        IconButton(onClick = onClick) {
            Box {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFF1E90FF),
                    modifier = Modifier.size(24.dp)
                )
                AnimatedVisibility(
                    visible = unreadCount > 0,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.Red),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}