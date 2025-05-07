package com.example.pbl5.ui.screens

import coil.compose.AsyncImage
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
                notifications = mainViewModel.notifications.value,
                onNotificationsUpdated = { mainViewModel.loadNotifications() }
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
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                raspberryPiData?.let { piData ->
                    FishStats(
                        totalFishCount = piData.totalFishCount,
                        deadFishCount = deadFishData?.count ?: 0
                    )
                } ?: run {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Chưa kết nối với Raspberry Pi",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Tiêu đề và bộ lọc
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Tiêu đề với icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Fish History",
                                tint = Color(0xFF1E90FF),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lịch sử cá chết",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        // Bộ lọc ngày
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { showDatePicker = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF1E90FF)
                                ),
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = "Calendar",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Chọn ngày")
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "Lọc theo: ${
                                    datePickerState.selectedDateMillis?.let {
                                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                                    } ?: "Tất cả"
                                }",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            if (datePickerState.selectedDateMillis != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(
                                    onClick = {
                                        datePickerState.selectedDateMillis = null
                                        fishViewModel.resetFilter()
                                        Log.d("FishScreen", "Đã xóa bộ lọc ngày")
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color(0xFF1E90FF)
                                    )
                                ) {
                                    Text("Xóa lọc")
                                }
                            }
                        }
                    }
                }
            }

            // Hiển thị trạng thái đang tải
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF1E90FF))
                    }
                }
            }
            // Hiển thị lỗi
            else if (errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = errorMessage ?: "Lỗi không xác định",
                                color = Color.Red,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            // Hiển thị khi không có Serial ID
            else if (mainViewModel.serialId.value.isBlank()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Serial ID không hợp lệ",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            // Hiển thị danh sách lịch sử cá chết
            else if (datePickerState.selectedDateMillis == null && deadFishHistory.isNotEmpty()) {
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
            }
            // Hiển thị danh sách đã lọc
            else if (filteredDeadFishHistory.isNotEmpty()) {
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
            }
            // Hiển thị khi không có dữ liệu
            else {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Không có lịch sử cá chết cho ngày đã chọn",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Dialog hiển thị chi tiết cá chết
        if (showModal && selectedImageUrl != null) {
            AlertDialog(
                onDismissRequest = { showModal = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                title = {
                    Text(
                        text = "Ảnh chụp cá chết",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E90FF)
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
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

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "Thời gian: ${
                                        selectedTimestamp?.let {
                                            SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(it)
                                        } ?: "N/A"
                                    }",
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Số lượng cá chết: ${selectedCount ?: 0}",
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showModal = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
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
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { selectedDate ->
                                fishViewModel.filterDeadFishHistoryByDate(selectedDate)
                                Log.d("FishScreen", "Ngày được chọn: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDate))}")
                            }
                            showDatePicker = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDatePicker = false },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF1E90FF)
                        )
                    ) {
                        Text("Hủy")
                    }
                },

            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black,
                        headlineContentColor = Color.Black,
                        weekdayContentColor = Color.Black,
                        dayContentColor = Color.Black,
                        selectedDayContentColor = Color.White,
                        selectedDayContainerColor = Color(0xFF1E90FF),
                        todayContentColor = Color(0xFF1E90FF),
                        todayDateBorderColor = Color(0xFF1E90FF)
                    )
                )
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Số lượng cá chết: $count",
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
            Button(
                onClick = onDetailClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Chi tiết")
            }
        }
    }
}