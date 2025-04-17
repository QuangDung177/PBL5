package com.example.pbl5.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pbl5.ui.components.*
import com.example.pbl5.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, navController: NavHostController) {
    val serialId by viewModel.serialId // Sử dụng by với MutableState
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val raspberryPiData by viewModel.raspberryPiData
    val deadFishData by viewModel.deadFishData
    val turbidityData by viewModel.turbidityData
    val turbidityDistribution by viewModel.turbidityDistribution
    val userDisplayName by viewModel.userDisplayName

    Scaffold(
        topBar = {
            AppTopBar(
                title = userDisplayName,
                onNotificationClick = { /* TODO: Mở màn hình thông báo */ }
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
            // Giữ nguyên SerialInput
            SerialInput(
                serialId = serialId,
                onSerialIdChange = { viewModel.serialId.value = it }, // Cập nhật serialId
                onConnectClick = { viewModel.connectToRaspberryPi() },
                isLoading = isLoading,
                errorMessage = errorMessage
            )

            raspberryPiData?.let { piData ->
                FishStats(
                    totalFishCount = piData.totalFishCount,
                    deadFishCount = deadFishData?.count ?: 0 // Sửa tham số deadFishData thành deadFishCount
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
}