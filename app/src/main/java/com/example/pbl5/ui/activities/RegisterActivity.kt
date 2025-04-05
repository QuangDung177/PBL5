package com.example.pbl5.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pbl5.ui.screens.RegisterScreen

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterScreen(
                onBackClick = { finish() },
                onRegisterSuccess = {
                    Log.d("RegisterActivity", "onRegisterSuccess called")
                    // Chuyển sang MainActivity và xóa stack điều hướng
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish() // Đóng LoginActivity
                },
                activity = this // Truyền Activity hiện tại
            )
        }
    }
}