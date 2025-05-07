package com.example.pbl5.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    val settingsViewModel = SettingsViewModel(mainViewModel = mainViewModel)
    val deadFishThreshold by settingsViewModel.deadFishThreshold.collectAsState()
    val turbidityThreshold by settingsViewModel.turbidityThreshold.collectAsState()
    val context = LocalContext.current

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            AppTopBar(
                title = mainViewModel.userDisplayName.value,
                notifications = mainViewModel.notifications.value,
                onNotificationsUpdated = { mainViewModel.loadNotifications() }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = "Settings",
                onTabSelected = { tab ->
                    navController.navigate(tab) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        backgroundColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tiêu đề trang
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color.White,
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Cài đặt thông báo",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E90FF)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Thiết lập ngưỡng cảnh báo",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Card cài đặt ngưỡng
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color.White,
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Thiết lập ngưỡng cảnh báo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    // Ngưỡng cá chết
                    OutlinedTextField(
                        value = deadFishThreshold,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                                settingsViewModel.updateDeadFishThreshold(newValue)
                            }
                        },
                        label = { Text("Ngưỡng cá chết để thông báo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF1E90FF),
                            unfocusedBorderColor = Color(0xFFDDDDDD),
                            cursorColor = Color(0xFF1E90FF)
                        ),
                        singleLine = true
                    )

                    // Ngưỡng độ đục nước
                    OutlinedTextField(
                        value = turbidityThreshold,
                        onValueChange = { newValue ->
                            if (newValue.matches(Regex("^\\d*\\.?\\d{0,1}$")) || newValue.isEmpty()) {
                                settingsViewModel.updateTurbidityThreshold(newValue)
                            }
                        },
                        label = { Text("Ngưỡng độ đục nước để thông báo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF1E90FF),
                            unfocusedBorderColor = Color(0xFFDDDDDD),
                            cursorColor = Color(0xFF1E90FF)
                        ),
                        singleLine = true
                    )

                    // Nút cập nhật
                    Button(
                        onClick = {
                            scope.launch {
                                val success = settingsViewModel.saveThresholdsToFirebase()
                                if (success) {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        message = "Cập nhật thành công",
                                        duration = SnackbarDuration.Short
                                    )
                                } else {
                                    scaffoldState.snackbarHostState.showSnackbar(
                                        message = "Cập nhật thất bại",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF1E90FF),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Cập nhật",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Spacer để đẩy nút đăng xuất xuống dưới
            Spacer(modifier = Modifier.weight(1f))

            // Nút đăng xuất
            Button(
                onClick = {
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Đăng xuất",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}