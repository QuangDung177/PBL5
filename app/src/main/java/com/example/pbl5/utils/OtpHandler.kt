package com.example.pbl5.utils

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class OtpHandler(
    private val auth: FirebaseAuth,
    private val activity: Activity,
    private val coroutineScope: CoroutineScope
) {
    // Trạng thái
    val verificationId: MutableState<String?> = mutableStateOf(null)
    val isOtpSent: MutableState<Boolean> = mutableStateOf(false)
    val errorMessage: MutableState<String?> = mutableStateOf(null)
    val resendCooldown: MutableState<Int> = mutableStateOf(30)
    val isResendEnabled: MutableState<Boolean> = mutableStateOf(true)
    val isLoading: MutableState<Boolean> = mutableStateOf(false)

    // Callback xử lý OTP
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            isLoading.value = false
            onVerificationCompletedCallback?.invoke(credential)
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
            Toast.makeText(activity, "OTP đã được gửi!", Toast.LENGTH_SHORT).show()

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

    // Callback để xử lý khi xác minh tự động hoàn tất
    private var onVerificationCompletedCallback: ((PhoneAuthCredential) -> Unit)? = null

    fun setOnVerificationCompletedCallback(callback: (PhoneAuthCredential) -> Unit) {
        onVerificationCompletedCallback = callback
    }

    // Gửi OTP
    fun sendOtp(phoneNumber: String) {
        isLoading.value = true
        val fullPhoneNumber = "+84${phoneNumber.trimStart('0')}"
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            fullPhoneNumber,
            60,
            TimeUnit.SECONDS,
            activity,
            callbacks
        )
    }
}