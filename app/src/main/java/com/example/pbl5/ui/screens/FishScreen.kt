package com.example.pbl5.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
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
    var showFullScreenImage by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedTimestamp by remember { mutableStateOf<Long?>(null) }
    var selectedCount by remember { mutableStateOf<Int?>(null) }

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(mainViewModel.serialId.value) {
        fishViewModel.loadDeadFishHistory()
        fishViewModel.resetFilter()
    }

    LaunchedEffect(deadFishHistory, filteredDeadFishHistory) {
        Log.d("FishScreen", "deadFishHistory size: ${deadFishHistory.size}")
        Log.d("FishScreen", "filteredDeadFishHistory size: ${filteredDeadFishHistory.size}")
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Phát hiện cá chết",
                notifications = mainViewModel.notifications.value,
                viewModel = mainViewModel,
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
            } else if (errorMessage != null) {
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
            } else if (mainViewModel.serialId.value.isBlank()) {
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
                                .height(200.dp)
                                .clickable { showFullScreenImage = true }
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

        if (showFullScreenImage && selectedImageUrl != null) {
            AlertDialog(
                onDismissRequest = { showFullScreenImage = false },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                    usePlatformDefaultWidth = false
                ),
                modifier = Modifier.fillMaxSize(),
                confirmButton = {},
                dismissButton = {},
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .clickable { showFullScreenImage = false }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = selectedImageUrl,
                                contentDescription = "Ảnh phóng to",
                                modifier = Modifier
                                    .fillMaxSize(),
                                alignment = Alignment.Center
                            )

                            IconButton(
                                onClick = { showFullScreenImage = false },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .size(36.dp)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Đóng",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = "Nhấn để đóng",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 16.dp)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
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
                }
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