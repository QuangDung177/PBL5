package com.example.pbl5.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.pbl5.ui.screens.HomeScreen

class HomeActivity : ComponentActivity() {
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
    }
}