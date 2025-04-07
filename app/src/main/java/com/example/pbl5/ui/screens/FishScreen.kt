package com.example.pbl5.ui.screens
import coil.compose.AsyncImage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pbl5.ui.components.AppTopBar
import com.example.pbl5.ui.components.BottomNavigationBar
import com.example.pbl5.ui.components.FishStats
import com.example.pbl5.ui.viewmodel.FishViewModel
import com.example.pbl5.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    fishViewModel: FishViewModel = FishViewModel(mainViewModel = mainViewModel)
) {
    val raspberryPiData by mainViewModel.raspberryPiData
    val deadFishData by mainViewModel.deadFishData
    val deadFishHistory by fishViewModel.deadFishHistory
    val errorMessage by fishViewModel.errorMessage

    // State để quản lý modal
    var showModal by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedTimestamp by remember { mutableStateOf<Long?>(null) }
    var selectedCount by remember { mutableStateOf<Int?>(null) }

    // Tải lịch sử cá chết khi vào màn hình
    LaunchedEffect(Unit) {
        fishViewModel.loadDeadFishHistory()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Fish",
                onNotificationClick = { /* TODO: Mở màn hình thông báo */ }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = "Fish",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
        ) {
            item {
                raspberryPiData?.let { piData ->
                    FishStats(
                        totalFishCount = piData.totalFishCount,
                        deadFishCount = deadFishData?.count ?: 0
                    )
                } ?: run {
                    Text(
                        text = "Chưa kết nối với Raspberry Pi",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            item {
                Text(
                    text = "Lịch sử cá chết",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (deadFishHistory.isNotEmpty()) {
                // Đổi màu nền của phần lịch sử cá chết thành trắng
                items(deadFishHistory) { history ->
                    DeadFishHistoryItem(
                        timestamp = history.timestamp?.time,
                        count = history.count,
                        onDetailClick = {
                            selectedImageUrl = history.imageUrl
                            selectedTimestamp = history.timestamp?.time
                            selectedCount = history.count
                            showModal = true
                        }
                    )
                }
            } else {
                item {
                    Text(
                        text = errorMessage ?: "Không có lịch sử cá chết",
                        color = Color.Gray,
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.White) // Đổi màu nền của thông báo thành trắng
                    )
                }
            }
        }

        // Modal hiển thị ảnh cá chết
        if (showModal && selectedImageUrl != null) {
            AlertDialog(
                onDismissRequest = { showModal = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Tăng chiều rộng modal (90% chiều rộng màn hình)
                    .wrapContentHeight(),
                title = { Text("Ảnh chụp cá chết") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White) // Đảm bảo màu nền modal là trắng
                            .padding(16.dp)
                    ) {
                        // Hiển thị ảnh
                        AsyncImage(
                            model = selectedImageUrl,
                            contentDescription = "Ảnh cá chết",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp) // Tăng chiều cao ảnh để dễ nhìn
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // Hiển thị thời gian
                        Text(
                            text = "Thời gian: ${
                                selectedTimestamp?.let {
                                    SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(it)
                                } ?: "N/A"
                            }",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        // Hiển thị số lượng cá chết
                        Text(
                            text = "Số lượng cá chết: ${selectedCount ?: 0}",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showModal = false }) {
                        Text("Đóng")
                    }
                },
                containerColor = Color.White // Đảm bảo màu nền của AlertDialog là trắng
            )
        }
    }
}

@Composable
fun DeadFishHistoryItem(
    timestamp: Long?,
    count: Int,
    modifier: Modifier = Modifier, // Thêm modifier để tùy chỉnh màu nền
    onDetailClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White // Đặt màu nền của Card thành trắng
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White) // Đảm bảo Row cũng có nền trắng
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Thời gian: ${
                        timestamp?.let {
                            SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(it)
                        } ?: "N/A"
                    }",
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = "Số lượng cá chết: $count",
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
            Button(
                onClick = onDetailClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
            ) {
                Text("Chi tiết")
            }
        }
    }
}