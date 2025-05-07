package com.example.pbl5.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pbl5.ui.components.*
import com.example.pbl5.ui.viewmodel.MainViewModel

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

    // Create a gradient background
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8FBFF),
            Color(0xFFF0F4FA)
        )
    )

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = userDisplayName,
                notifications = notifications,
                onNotificationsUpdated = { viewModel.loadNotifications() }
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
        },
        containerColor = Color.Transparent // Make scaffold background transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                // Serial input section
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

                // Show data only if connected
                raspberryPiData?.let { piData ->
                    Spacer(modifier = Modifier.height(8.dp))

                    // Fish statistics card
                    FishStats(
                        totalFishCount = piData.totalFishCount,
                        deadFishCount = deadFishData?.count ?: 0
                    )

                    // Turbidity statistics
                    turbidityData?.let { turbidity ->
                        TurbidityStats(
                            value = turbidity.value,
                            status = turbidity.status,
                            timestamp = turbidity.timestamp?.time,
                            deviceStatus = piData.status
                        )
                    } ?: run {
                        EmptyStateCard(
                            message = "Chưa có dữ liệu độ đục nước",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Turbidity chart
                    TurbidityChart(distribution = turbidityDistribution)

                    // Add some space at the bottom
                    Spacer(modifier = Modifier.height(16.dp))
                } ?: run {
                    // Show empty state when not connected
                    EmptyStateCard(
                        message = "Chưa kết nối với Raspberry Pi",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}