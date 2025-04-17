package com.example.pbl5.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.pbl5.ui.screens.HomeScreen
import com.example.pbl5.workers.ThresholdCheckWorker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.TimeUnit

class HomeActivity : ComponentActivity() {
    private val firestore = FirebaseFirestore.getInstance()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Permissions", "Quyền POST_NOTIFICATIONS được cấp")
        } else {
            Log.w("Permissions", "Quyền POST_NOTIFICATIONS bị từ chối")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeScreen(
                onLoginClick = {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                },
                onRegisterClick = {
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                }
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        saveFcmToken()
        scheduleThresholdCheck()
    }

    private fun saveFcmToken() {
        val deviceId = generateDeviceId()
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Lỗi khi lấy FCM token: ${task.exception}")
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "FCM Token: $token")

            firestore.collection("DEVICES")
                .document(deviceId)
                .set(mapOf("fcmToken" to token, "lastUpdated" to System.currentTimeMillis()))
                .addOnSuccessListener {
                    Log.d("FCM", "Lưu FCM token thành công cho device: $deviceId")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Lỗi khi lưu FCM token: ${e.message}")
                }
        }
    }

    private fun generateDeviceId(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }


    private fun scheduleThresholdCheck() {
        val workRequest = PeriodicWorkRequestBuilder<ThresholdCheckWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "thresholdCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        Log.d("WorkManager", "Đã lên lịch kiểm tra ngưỡng định kỳ")
    }
}