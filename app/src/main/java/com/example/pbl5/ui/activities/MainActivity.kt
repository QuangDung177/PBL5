package com.example.pbl5.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.example.pbl5.data.RaspberryPiRepository
import com.example.pbl5.ui.navigation.AppNavigation
import com.example.pbl5.ui.viewmodel.MainViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val firestore = FirebaseFirestore.getInstance()
    private val repository = RaspberryPiRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            AppNavigation(
                navController = navController,
                viewModel = viewModel
            )
        }

        // Lấy và lưu FCM token
        saveFcmToken()

        // Kiểm tra ngưỡng và gửi thông báo
        checkThresholds()
    }

    private fun saveFcmToken() {
        val userPhone = getUserPhoneNumber()
        if (userPhone.isEmpty()) {
            Log.e("FCM", "Không thể lấy số điện thoại của người dùng")
            return
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Lỗi khi lấy FCM token: ${task.exception}")
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "FCM Token: $token")

            firestore.collection("USERS")
                .document(userPhone)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "Lưu FCM token thành công cho user: $userPhone")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Lỗi khi lưu FCM token: ${e.message}")
                }
        }
    }

    private fun checkThresholds() {
        // Thay "your_serial_id" bằng serialId thực tế của Raspberry Pi
        val serialId = "your_serial_id"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.checkAndNotify(serialId)
                Log.d("MainActivity", "Kiểm tra ngưỡng thành công")
            } catch (e: Exception) {
                Log.e("MainActivity", "Lỗi khi kiểm tra ngưỡng: ${e.message}")
            }
        }
    }

    private fun getUserPhoneNumber(): String {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val rawPhoneNumber = currentUser?.phoneNumber?.removePrefix("+84") ?: ""
        return if (rawPhoneNumber.isNotEmpty()) "0$rawPhoneNumber" else ""
    }
}