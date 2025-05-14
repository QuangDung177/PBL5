package com.example.pbl5.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

    // State for dialog
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    // Cải thiện AlertDialog với animation và thiết kế thân thiện hơn
    @Composable
    fun EnhancedAlertDialog(
        message: String,
        isSuccess: Boolean,
        onDismiss: () -> Unit
    ) {
        Dialog(onDismissRequest = { onDismiss() }) {
            // Animation khi hiển thị dialog
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(initialScale = 0.8f) + fadeIn(),
                exit = scaleOut(targetScale = 0.8f) + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color.White,
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Animation cho icon
                        val infiniteTransition = rememberInfiniteTransition()
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800),
                                repeatMode = RepeatMode.Reverse
                            )
                        )

                        // Icon với hiệu ứng nhấp nháy
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSuccess) Color(0xFF4CAF50).copy(alpha = 0.1f)
                                    else Color.Red.copy(alpha = 0.1f)
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = if (isSuccess) "Success" else "Error",
                                tint = if (isSuccess) Color(0xFF4CAF50) else Color.Red,
                                modifier = Modifier.scale(scale)
                            )
                        }

                        // Tiêu đề
                        Text(
                            text = if (isSuccess) "Thành công" else "Lỗi",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSuccess) Color(0xFF4CAF50) else Color.Red
                        )

                        // Nội dung thông báo
                        Text(
                            text = message,
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // Nút đóng với hiệu ứng ripple
                        Button(
                            onClick = { onDismiss() },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (isSuccess) Color(0xFF4CAF50) else Color.Red,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Đóng",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

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
                                dialogMessage = if (success) "Cập nhật ngưỡng thành công" else "Cập nhật ngưỡng cảnh báo thất bại"
                                isSuccess = success
                                showDialog = true
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
                    .align(Alignment.End) // Căn sang góc phải
                    .wrapContentWidth() // Độ rộng vừa với nội dung
                    .height(60.dp)
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Đăng xuất",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // Hiển thị dialog cải tiến khi cần
        if (showDialog) {
            EnhancedAlertDialog(
                message = dialogMessage,
                isSuccess = isSuccess,
                onDismiss = { showDialog = false }
            )
        }
    }
}