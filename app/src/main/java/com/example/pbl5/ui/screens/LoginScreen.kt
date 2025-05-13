package com.example.pbl5.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pbl5.R
import com.example.pbl5.utils.FirebaseManager
import com.example.pbl5.utils.OtpHandler
import com.example.pbl5.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    activity: Activity
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()

    // Quản lý trạng thái
    val phoneNumber = remember { mutableStateOf("") }
    val otp = remember { mutableStateOf("") }
    val firebaseManager = remember { FirebaseManager() }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Khởi tạo OtpHandler và FirebaseManager
    val otpHandler = remember {
        OtpHandler(auth, activity, coroutineScope).apply {
            setOnVerificationCompletedCallback { credential ->
                firebaseManager.verifyOtp(
                    credential = credential,
                    onSuccess = {
                        onLoginSuccess()
                    },
                    onFailure = { error ->
                        errorMessage = error
                        showErrorDialog = true
                    }
                )
            }
        }
    }

    // Custom Error Dialog
    @Composable
    fun CustomErrorDialog(
        message: String,
        onDismiss: () -> Unit
    ) {
        Dialog(onDismissRequest = { onDismiss() }) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Lỗi",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Text(
                            text = message,
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Button(
                            onClick = { onDismiss() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.fish_icon),
                contentDescription = "Fish logo",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 30.dp)
            )
            Text(
                text = "Đăng nhập",
                fontSize = 24.sp,
                color = Color(0xFF333333),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            if (!otpHandler.isOtpSent.value) {
                Text(
                    text = "Vui lòng nhập số điện thoại để nhận OTP.",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 40.dp)
                        .widthIn(max = 300.dp)
                )
                OutlinedTextField(
                    value = phoneNumber.value,
                    onValueChange = { phoneNumber.value = it },
                    label = { Text("Số điện thoại (+84)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                Row(
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp)
                        .padding(bottom = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (otpHandler.isLoading.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            color = Color(0xFF1E90FF)
                        )
                    } else {
                        Button(
                            onClick = {
                                if (phoneNumber.value.isNotEmpty()) {
                                    if (Utils.isValidPhoneNumber(phoneNumber.value)) {
                                        firebaseManager.checkPhoneNumberForLogin(
                                            phoneNumber = phoneNumber.value,
                                            onSuccess = { otpHandler.sendOtp(phoneNumber.value) },
                                            onNotFound = {
                                                errorMessage = "Số điện thoại chưa được đăng ký!"
                                                showErrorDialog = true
                                            },
                                            onFailure = { error ->
                                                errorMessage = error
                                                showErrorDialog = true
                                            }
                                        )
                                    } else {
                                        errorMessage = "Số điện thoại phải có 10 chữ số!"
                                        showErrorDialog = true
                                    }
                                } else {
                                    errorMessage = "Vui lòng nhập số điện thoại"
                                    showErrorDialog = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Gửi OTP", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Text(
                    text = "Nhập mã OTP được gửi đến ${phoneNumber.value}.",
                    fontSize = 16.sp,
                    color = Color(0xFF666666),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 40.dp)
                        .widthIn(max = 300.dp)
                )
                OutlinedTextField(
                    value = otp.value,
                    onValueChange = { otp.value = it },
                    label = { Text("Mã OTP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                Button(
                    onClick = {
                        if (otp.value.isNotEmpty() && otpHandler.verificationId.value != null) {
                            val credential = PhoneAuthProvider.getCredential(otpHandler.verificationId.value!!, otp.value)
                            firebaseManager.verifyOtp(
                                credential = credential,
                                onSuccess = {
                                    onLoginSuccess()
                                },
                                onFailure = { error ->
                                    errorMessage = error
                                    showErrorDialog = true
                                }
                            )
                        } else {
                            errorMessage = "Vui lòng nhập OTP"
                            showErrorDialog = true
                        }
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp)
                        .padding(bottom = 15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Xác nhận OTP", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp)
                        .padding(bottom = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (otpHandler.isLoading.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            color = Color(0xFF1E90FF)
                        )
                    } else {
                        Button(
                            onClick = { otpHandler.sendOtp(phoneNumber.value) },
                            enabled = otpHandler.isResendEnabled.value,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (otpHandler.isResendEnabled.value) Color(0xFF1E90FF) else Color.Gray,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (otpHandler.isResendEnabled.value) "Gửi lại OTP" else "Gửi lại sau ${otpHandler.resendCooldown.value}s",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp)
                    .padding(bottom = 15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E90FF)
                ),
                border = BorderStroke(1.dp, Color(0xFF1E90FF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Quay lại", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Show error dialog if needed
        if (showErrorDialog) {
            CustomErrorDialog(
                message = errorMessage,
                onDismiss = { showErrorDialog = false }
            )
        }
    }
}