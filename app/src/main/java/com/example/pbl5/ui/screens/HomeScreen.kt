package com.example.pbl5.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pbl5.R

@Composable
fun HomeScreen(onLoginClick: () -> Unit, onRegisterClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.fish_icon),
                contentDescription = "Fish logo",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 30.dp)
            )

            Text(
                text = "Hồ nuôi cá thông minh",
                fontSize = 24.sp,
                color = Color(0xFF333333),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Text(
                text = "Với AI và IoT, chúng tôi cung cấp công cụ giám sát và phát hiện cá chết, giúp bạn duy trì một hồ cá khỏe mạnh.",
                fontSize = 16.sp,
                color = Color(0xFF666666),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 40.dp)
                    .widthIn(max = 300.dp)
            )

            Button(
                onClick = onLoginClick, // Gọi hàm chuyển sang Login
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp)
                    .padding(bottom = 15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Đăng nhập", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onRegisterClick, // Gọi hàm chuyển sang Register
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp)
                    .padding(bottom = 15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E90FF)
                ),
                border = BorderStroke(1.dp, Color(0xFF1E90FF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Đăng ký", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}