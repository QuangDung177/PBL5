package com.example.pbl5.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pbl5.ui.components.FishStats
import com.example.pbl5.ui.components.SerialInput
import com.example.pbl5.ui.components.TurbidityChart
import com.example.pbl5.ui.components.TurbidityStats
import com.example.pbl5.ui.viewmodel.MainViewModel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val serialId by viewModel.serialId
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val raspberryPiData by viewModel.raspberryPiData
    val deadFishData by viewModel.deadFishData
    val turbidityData by viewModel.turbidityData
    val turbidityDistribution by viewModel.turbidityDistribution
    val userDisplayName by viewModel.userDisplayName

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = userDisplayName,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Mở màn hình thông báo */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFF1E90FF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = { /* TODO: Điều hướng */ }) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Home",
                            tint = Color(0xFF1E90FF)
                        )
                    }
                    IconButton(onClick = { /* TODO: Điều hướng */ }) {
                        Icon(
                            imageVector = Icons.Filled.CenterFocusStrong,
                            contentDescription = "Fish",
                            tint = Color(0xFF1E90FF)
                        )
                    }
                    IconButton(onClick = { /* TODO: Điều hướng */ }) {
                        Icon(
                            imageVector = Icons.Filled.Sensors,
                            contentDescription = "Monitor",
                            tint = Color(0xFF1E90FF)
                        )
                    }
                    IconButton(onClick = { /* TODO: Điều hướng */ }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF1E90FF)
                        )
                    }
                }
            }
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
                isLoading = isLoading,
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
                    Text("Chưa có dữ liệu độ đục nước", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
                TurbidityChart(distribution = turbidityDistribution)
            } ?: run {
                Text("Chưa kết nối với Raspberry Pi", color = Color.Gray, modifier = Modifier.padding(16.dp))
            }
        }
    }
}