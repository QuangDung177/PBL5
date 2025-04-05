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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit,
    activity: Activity
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    // Quản lý trạng thái
    val phoneNumber = remember { mutableStateOf("") }
    val userName = remember { mutableStateOf("") }
    val otp = remember { mutableStateOf("") }
    val verificationId = remember { mutableStateOf<String?>(null) }
    val isOtpSent = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val resendCooldown = remember { mutableStateOf(30) }
    val isResendEnabled = remember { mutableStateOf(true) }
    val isLoading = remember { mutableStateOf(false) } // Trạng thái loading

    // Callback để xử lý gửi OTP
    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                isLoading.value = false
                signInOrRegisterWithPhoneAuthCredential(credential, auth, firestore, phoneNumber.value, userName.value, context, onRegisterSuccess)
            }

            override fun onVerificationFailed(exception: com.google.firebase.FirebaseException) {
                isLoading.value = false
                errorMessage.value = "Gửi OTP thất bại: ${exception.message}"
            }

            override fun onCodeSent(verId: String, token: PhoneAuthProvider.ForceResendingToken) {
                isLoading.value = false
                verificationId.value = verId
                isOtpSent.value = true
                errorMessage.value = null
                Toast.makeText(context, "OTP đã được gửi!", Toast.LENGTH_SHORT).show()

                // Bắt đầu đếm ngược để gửi lại OTP
                coroutineScope.launch {
                    isResendEnabled.value = false
                    resendCooldown.value = 30
                    while (resendCooldown.value > 0) {
                        delay(1000L)
                        resendCooldown.value -= 1
                    }
                    isResendEnabled.value = true
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
                text = "Đăng ký",
                fontSize = 24.sp,
                color = Color(0xFF333333),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            if (!isOtpSent.value) {
                Text(
                    text = "Vui lòng nhập thông tin để đăng ký.",
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
                OutlinedTextField(
                    value = userName.value,
                    onValueChange = { userName.value = it },
                    label = { Text("Tên hiển thị") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                errorMessage.value?.let { message ->
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
                    if (isLoading.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            color = Color(0xFF1E90FF)
                        )
                    } else {
                        Button(
                            onClick = {
                                if (phoneNumber.value.isNotEmpty() && userName.value.isNotEmpty()) {
                                    if (isValidPhoneNumber(phoneNumber.value)) {
                                        firestore.collection("USERS").document(phoneNumber.value).get()
                                            .addOnSuccessListener { document ->
                                                if (!document.exists()) {
                                                    isLoading.value = true
                                                    val fullPhoneNumber = "+84${phoneNumber.value.trimStart('0')}"
                                                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                                        fullPhoneNumber,
                                                        60,
                                                        TimeUnit.SECONDS,
                                                        activity,
                                                        callbacks
                                                    )
                                                } else {
                                                    errorMessage.value = "Số điện thoại đã được đăng ký!"
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage.value = "Lỗi kiểm tra số điện thoại: ${e.message}"
                                            }
                                    } else {
                                        errorMessage.value = "Số điện thoại phải có 10 chữ số!"
                                    }
                                } else {
                                    errorMessage.value = "Vui lòng nhập đầy đủ thông tin"
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
                errorMessage.value?.let { message ->
                    Text(
                        text = message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
                Button(
                    onClick = {
                        if (otp.value.isNotEmpty() && verificationId.value != null) {
                            val credential = PhoneAuthProvider.getCredential(verificationId.value!!, otp.value)
                            signInOrRegisterWithPhoneAuthCredential(credential, auth, firestore, phoneNumber.value, userName.value, context, onRegisterSuccess)
                        } else {
                            errorMessage.value = "Vui lòng nhập OTP"
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
                // Nút gửi lại OTP với CircularProgressIndicator
                Row(
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp)
                        .padding(bottom = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLoading.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            color = Color(0xFF1E90FF)
                        )
                    } else {
                        Button(
                            onClick = {
                                isLoading.value = true
                                val fullPhoneNumber = "+84${phoneNumber.value.trimStart('0')}"
                                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                    fullPhoneNumber,
                                    60,
                                    TimeUnit.SECONDS,
                                    activity,
                                    callbacks
                                )
                            },
                            enabled = isResendEnabled.value,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isResendEnabled.value) Color(0xFF1E90FF) else Color.Gray,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isResendEnabled.value) "Gửi lại OTP" else "Gửi lại sau ${resendCooldown.value}s",
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

private fun isValidPhoneNumber(phone: String): Boolean {
    return phone.length == 10 && phone.all { it.isDigit() }
}

private fun signInOrRegisterWithPhoneAuthCredential(
    credential: PhoneAuthCredential,
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    phoneNumber: String,
    userName: String,
    context: android.content.Context,
    onRegisterSuccess: () -> Unit
) {
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userData = hashMapOf(
                    "userName" to userName,
                    "creatAt" to System.currentTimeMillis().toString()
                )
                firestore.collection("USERS").document(phoneNumber)
                    .set(userData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                        onRegisterSuccess()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Lưu dữ liệu thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Xác minh OTP thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}