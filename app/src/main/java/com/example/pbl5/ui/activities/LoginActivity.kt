package com.example.pbl5.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pbl5.ui.screens.LoginScreen

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen(
                onBackClick = {
                    // Đóng LoginActivity và quay lại HomeActivity
                    finish()
                }
            )
        }
    }
}