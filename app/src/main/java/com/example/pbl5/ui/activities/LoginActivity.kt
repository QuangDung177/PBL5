package com.example.pbl5.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pbl5.ui.screens.LoginScreen

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen(
                onBackClick = { finish() },
                onLoginSuccess = {
                    Log.d("LoginActivity", "onLoginSuccess called")
                    // Chuyển sang MainActivity và xóa stack điều hướng
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish() // Đóng LoginActivity
                },
                activity = this
            )
        }
    }
}