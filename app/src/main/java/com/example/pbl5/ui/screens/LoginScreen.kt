package com.example.pbl5.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pbl5.R
import com.example.pbl5.utils.FirebaseManager
import com.example.pbl5.utils.OtpHandler
import com.example.pbl5.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

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
    // Khởi tạo OtpHandler và FirebaseManager
    val otpHandler = remember {
        OtpHandler(auth, activity, coroutineScope).apply {
            setOnVerificationCompletedCallback { credential ->
                firebaseManager.verifyOtp(
                    credential = credential,
                    onSuccess = {
                        Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    },
                    onFailure = { error -> errorMessage.value = error }
                )
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
                otpHandler.errorMessage.value?.let { message ->
                    Text(
                        text = message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
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
                            onClick = {
                                if (phoneNumber.value.isNotEmpty()) {
                                    if (Utils.isValidPhoneNumber(phoneNumber.value)) {
                                        firebaseManager.checkPhoneNumberForLogin(
                                            phoneNumber = phoneNumber.value,
                                            onSuccess = { otpHandler.sendOtp(phoneNumber.value) },
                                            onNotFound = { otpHandler.errorMessage.value = "Số điện thoại chưa được đăng ký!" },
                                            onFailure = { error -> otpHandler.errorMessage.value = error }
                                        )
                                    } else {
                                        otpHandler.errorMessage.value = "Số điện thoại phải có 10 chữ số!"
                                    }
                                } else {
                                    otpHandler.errorMessage.value = "Vui lòng nhập số điện thoại"
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
                otpHandler.errorMessage.value?.let { message ->
                    Text(
                        text = message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
                Button(
                    onClick = {
                        if (otp.value.isNotEmpty() && otpHandler.verificationId.value != null) {
                            val credential = PhoneAuthProvider.getCredential(otpHandler.verificationId.value!!, otp.value)
                            firebaseManager.verifyOtp(
                                credential = credential,
                                onSuccess = {
                                    Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                },
                                onFailure = { error -> otpHandler.errorMessage.value = error }
                            )
                        } else {
                            otpHandler.errorMessage.value = "Vui lòng nhập OTP"
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
    }
}