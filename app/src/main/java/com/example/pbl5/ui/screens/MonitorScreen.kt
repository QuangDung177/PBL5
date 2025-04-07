package com.example.pbl5.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pbl5.ui.components.*
import com.example.pbl5.ui.viewmodel.MainViewModel
import com.example.pbl5.ui.viewmodel.MonitorViewModel
import androidx.compose.ui.Alignment
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    monitorViewModel: MonitorViewModel = MonitorViewModel(mainViewModel = mainViewModel)
) {
    val isLoading by monitorViewModel.isLoading
    val errorMessage by monitorViewModel.errorMessage
    val turbidityData by monitorViewModel.turbidityData
    val turbidityDistribution by monitorViewModel.turbidityDistribution
    val turbidityHistory by monitorViewModel.turbidityHistory

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Độ đục nước",
                onNotificationClick = { /* TODO: Mở màn hình thông báo */ }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = "Monitor",
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
                .background(Color.White)
                .padding(padding)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
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
                        text = "Vui lòng nhập Serial ID từ màn hình chính",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (turbidityData != null) {
                item {
                    TurbidityStats(
                        value = turbidityData!!.value,
                        status = turbidityData!!.status,
                        timestamp = turbidityData!!.timestamp?.time,
                        deviceStatus = mainViewModel.raspberryPiData.value?.status ?: "N/A"
                    )
                }

                item {
                    TurbidityChart(distribution = turbidityDistribution)
                }

                item {
                    TurbidityHistoryChart(history = turbidityHistory)
                }
            } else {
                item {
                    Text(
                        text = "Không có dữ liệu độ đục nước",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}