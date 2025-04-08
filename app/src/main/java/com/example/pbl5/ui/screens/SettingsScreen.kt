package com.example.pbl5.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.pbl5.ui.activities.LoginActivity
import com.example.pbl5.ui.components.AppTopBar
import com.example.pbl5.ui.components.BottomNavigationBar
import com.example.pbl5.ui.viewmodel.MainViewModel
import com.example.pbl5.ui.viewmodel.SettingsViewModel
import android.content.Intent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    // Khởi tạo SettingsViewModel với MainViewModel
    val settingsViewModel = SettingsViewModel(mainViewModel = mainViewModel)

    // Lấy giá trị từ SettingsViewModel
    val deadFishThreshold by settingsViewModel.deadFishThreshold.collectAsState()
    val turbidityThreshold by settingsViewModel.turbidityThreshold.collectAsState()

    // Lấy context để khởi tạo Intent khi đăng xuất
    val context = LocalContext.current

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings",
                onNotificationClick = {
                    navController.navigate("Notifications")
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = "Settings",
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Phần cài đặt ngưỡng
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "Cài đặt thông báo",
                    fontSize = 24.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Ngưỡng cá chết
                OutlinedTextField(
                    value = deadFishThreshold,
                    onValueChange = { newValue ->
                        // Chỉ cho phép nhập số
                        if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                            settingsViewModel.updateDeadFishThreshold(newValue)
                        }
                    },
                    label = { Text("Ngưỡng cá chết để thông báo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Ngưỡng độ đục nước
                OutlinedTextField(
                    value = turbidityThreshold,
                    onValueChange = { newValue ->
                        // Chỉ cho phép nhập số
                        if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                            settingsViewModel.updateTurbidityThreshold(newValue)
                        }
                    },
                    label = { Text("Ngưỡng độ đục nước để thông báo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Nút Cập nhật
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            settingsViewModel.saveThresholdsToFirebase()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
                    ) {
                        Text(
                            text = "Cập nhật",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Nút Đăng xuất
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        // Chuyển về LoginActivity và xóa stack điều hướng
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(
                        text = "Đăng xuất",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}