package com.example.pbl5.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pbl5.ui.screens.RegisterScreen

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterScreen(
                onBackClick = {
                    // Đóng RegisterActivity và quay lại HomeActivity
                    finish()
                }
            )
        }
    }
}