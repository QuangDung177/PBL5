package com.example.pbl5.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pbl5.ui.components.*
import com.example.pbl5.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, navController: NavHostController) {
    val serialId by viewModel.serialId
    val isLoading by viewModel.isLoading
    val isRefreshing by viewModel.isRefreshing
    val errorMessage by viewModel.errorMessage
    val isConnected by viewModel.isConnected
    val raspberryPiData by viewModel.raspberryPiData
    val deadFishData by viewModel.deadFishData
    val turbidityData by viewModel.turbidityData
    val turbidityDistribution by viewModel.turbidityDistribution
    val userDisplayName by viewModel.userDisplayName
    val notifications by viewModel.notifications
    var showNotificationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = userDisplayName,
                onNotificationClick = { showNotificationDialog = true }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = "Home",
                onTabSelected = { tab ->
                    navController.navigate(tab) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
        ) {
            SerialInput(
                serialId = serialId,
                onSerialIdChange = { viewModel.serialId.value = it },
                onConnectClick = { viewModel.connectToRaspberryPi() },
                onRefreshClick = { viewModel.refreshData() },
                isLoading = isLoading,
                isRefreshing = isRefreshing,
                isConnected = isConnected,
                errorMessage = errorMessage
            )

            raspberryPiData?.let { piData ->
                FishStats(
                    totalFishCount = piData.totalFishCount,
                    deadFishCount = deadFishData?.count ?: 0
                )
                turbidityData?.let { turbidity ->
                    TurbidityStats(
                        value = turbidity.value,
                        status = turbidity.status,
                        timestamp = turbidity.timestamp?.time,
                        deviceStatus = piData.status
                    )
                } ?: run {
                    Text(
                        text = "Chưa có dữ liệu độ đục nước",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                TurbidityChart(distribution = turbidityDistribution)
            } ?: run {
                Text(
                    text = "Chưa kết nối với Raspberry Pi",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    // Dialog thông báo
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications Icon",
                        tint = Color(0xFF1E90FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Thông báo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E90FF)
                    )
                }
            },
            text = {
                if (notifications.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "No Notifications",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
                            .padding(horizontal = 8.dp)
                    ) {
                        itemsIndexed(notifications) { index, notification ->
                            NotificationItem(notification = notification)
                            if (index < notifications.size - 1) {
                                Divider(
                                    color = Color(0xFFE0E0E0),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Đóng", color = Color(0xFF1E90FF), fontSize = 16.sp)
                }
            },
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        )
    }
}

// Composable cho mỗi mục thông báo
@Composable
fun NotificationItem(notification: com.example.pbl5.data.NotificationData) {
    // Tính thời gian tương đối
    val timeDiff = notification.timestamp?.let { timestamp ->
        val currentTime = System.currentTimeMillis()
        val diffMillis = currentTime - timestamp.time
        val diffMinutes = diffMillis / (1000 * 60)
        when {
            diffMinutes < 60 -> "$diffMinutes phút trước"
            diffMinutes < 1440 -> "${diffMinutes / 60} giờ trước"
            else -> {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                dateFormat.format(timestamp)
            }
        }
    } ?: "Không xác định"

    // Xác định màu sắc dựa trên nội dung thông báo
    val backgroundColor = when {
        notification.message.contains("Xấu", ignoreCase = true) ||
                notification.message.contains("cá chết", ignoreCase = true) -> Color(0xFFFFF0F0) // Màu đỏ nhạt cho cảnh báo
        else -> Color.White
    }

    // Trạng thái để làm nổi bật khi nhấn
    var isPressed by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                clip = true
            )
            .background(
                color = if (isPressed) backgroundColor.copy(alpha = 0.7f) else backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                onClick = { isPressed = !isPressed }
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notification Icon",
            tint = if (backgroundColor == Color.White) Color(0xFF1E90FF) else Color(0xFFD32F2F),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = notification.message,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
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