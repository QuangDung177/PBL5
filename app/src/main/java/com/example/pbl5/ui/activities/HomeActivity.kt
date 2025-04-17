package com.example.pbl5.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.example.pbl5.ui.screens.HomeScreen

class HomeActivity : ComponentActivity() {
    // Launcher để yêu cầu quyền và xử lý kết quả
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Permissions", "Quyền POST_NOTIFICATIONS được cấp")
        } else {
            Log.w("Permissions", "Quyền POST_NOTIFICATIONS bị từ chối")
            // Xử lý trường hợp người dùng từ chối quyền (ví dụ: hiển thị thông báo giải thích)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeScreen(
                onLoginClick = {
                    // Chuyển sang LoginActivity
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                },
                onRegisterClick = {
                    // Chuyển sang RegisterActivity
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                }
            )
        }

        // Kiểm tra và yêu cầu quyền POST_NOTIFICATIONS trên Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}