package com.example.pbl5.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pbl5.data.NotificationData
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationDialog(
    notifications: List<NotificationData>,
    onDismiss: () -> Unit,
    onMarkAsRead: (String) -> Unit
) {
    val unreadCount = notifications.count { !it.isRead }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp)),
            color = Color.White,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications Icon",
                        tint = Color(0xFF1E90FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Thông báo${if (unreadCount > 0) " ($unreadCount chưa đọc)" else ""}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E90FF)
                    )
                }

                if (notifications.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(Color(0xFF1E90FF).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "No Notifications",
                                tint = Color(0xFF1E90FF),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Không có thông báo nào.",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                    ) {
                        itemsIndexed(notifications) { index, notification ->
                            NotificationItem(
                                message = notification.message,
                                timestamp = notification.timestamp?.time,
                                isRead = notification.isRead,
                                onClick = { onMarkAsRead(notification.id) }
                            )
                            if (index < notifications.size - 1) {
                                Divider(
                                    color = Color(0xFFE0E0E0),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF1E90FF),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            "Đóng",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    message: String,
    timestamp: Long?,
    isRead: Boolean,
    onClick: () -> Unit
) {
    val timeDiff = timestamp?.let { ts ->
        val currentTime = System.currentTimeMillis()
        val diffMillis = currentTime - ts
        val diffMinutes = diffMillis / (1000 * 60)
        when {
            diffMinutes < 60 -> "$diffMinutes phút trước"
            diffMinutes < 1440 -> "${diffMinutes / 60} giờ trước"
            else -> {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                dateFormat.format(Date(ts))
            }
        }
    } ?: "Không xác định"

    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = {
                if (!isRead) onClick()
                isPressed = !isPressed
            }),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = if (isPressed) Color(0xFFF5F5F5) else if (!isRead) Color(0xFFFFF3F3) else Color.White,
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (!isRead) Color(0xFFFFE0E0) else Color(0xFF1E90FF).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notification Icon",
                    tint = if (!isRead) Color.Red else Color(0xFF1E90FF),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontWeight = if (!isRead) FontWeight.Bold else FontWeight.Medium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeDiff,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}