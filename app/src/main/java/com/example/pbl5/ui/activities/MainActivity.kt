package com.example.pbl5.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.pbl5.ui.navigation.AppNavigation
import com.example.pbl5.ui.viewmodel.MainViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    private val firestore = FirebaseFirestore.getInstance()

    // Tạo ViewModel với context
    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(this@MainActivity) as T
            }
        }
    }

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

    private fun getUserPhoneNumber(): String {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val rawPhoneNumber = currentUser?.phoneNumber?.removePrefix("+84") ?: ""
        return if (rawPhoneNumber.isNotEmpty()) "0$rawPhoneNumber" else ""
    }
}