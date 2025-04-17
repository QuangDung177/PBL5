package com.example.pbl5.ui.screens

import coil.compose.AsyncImage
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    fishViewModel: FishViewModel
) {
    val raspberryPiData by mainViewModel.raspberryPiData
    val deadFishData by mainViewModel.deadFishData
    val deadFishHistory by fishViewModel.deadFishHistory
    val filteredDeadFishHistory by fishViewModel.filteredDeadFishHistory
    val errorMessage by fishViewModel.errorMessage
    val isLoading by fishViewModel.isLoading

    var showModal by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedTimestamp by remember { mutableStateOf<Long?>(null) }
    var selectedCount by remember { mutableStateOf<Int?>(null) }

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    // Tải lại lịch sử cá chết và reset bộ lọc khi serialId thay đổi
    LaunchedEffect(mainViewModel.serialId.value) {
        fishViewModel.loadDeadFishHistory()
        fishViewModel.resetFilter()
    }

    // Ghi log để kiểm tra dữ liệu
    LaunchedEffect(deadFishHistory, filteredDeadFishHistory) {
        Log.d("FishScreen", "deadFishHistory size: ${deadFishHistory.size}")
        Log.d("FishScreen", "filteredDeadFishHistory size: ${filteredDeadFishHistory.size}")
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Phát hiện cá chết",
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
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { showDatePicker = true }) {
                        Text("Chọn ngày")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lọc theo: ${
                            datePickerState.selectedDateMillis?.let {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                            } ?: "Tất cả"
                        }",
                        fontSize = 16.sp
                    )
                    if (datePickerState.selectedDateMillis != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis = null
                            fishViewModel.resetFilter()
                            Log.d("FishScreen", "Đã xóa bộ lọc ngày")
                        }) {
                            Text("Xóa lọc")
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage ?: "Lỗi không xác định",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (mainViewModel.serialId.value.isBlank()) {
                item {
                    Text(
                        text = "Serial ID không hợp lệ",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (datePickerState.selectedDateMillis == null && deadFishHistory.isNotEmpty()) {
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
            } else if (filteredDeadFishHistory.isNotEmpty()) {
                items(filteredDeadFishHistory) { history ->
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
                        text = "Không có lịch sử cá chết cho ngày đã chọn",
                        color = Color.Gray,
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.White)
                    )
                }
            }
        }

        if (showModal && selectedImageUrl != null) {
            AlertDialog(
                onDismissRequest = { showModal = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                title = { Text("Ảnh chụp cá chết") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        AsyncImage(
                            model = selectedImageUrl,
                            contentDescription = "Ảnh cá chết",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Thời gian: ${
                                selectedTimestamp?.let {
                                    SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(it)
                                } ?: "N/A"
                            }",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
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
                containerColor = Color.White
            )
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { selectedDate ->
                                fishViewModel.filterDeadFishHistoryByDate(selectedDate)
                                Log.d("FishScreen", "Ngày được chọn: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDate))}")
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Hủy")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun DeadFishHistoryItem(
    timestamp: Long?,
    count: Int,
    modifier: Modifier = Modifier,
    onDetailClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
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