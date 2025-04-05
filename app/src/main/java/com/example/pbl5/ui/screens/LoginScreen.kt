package com.example.pbl5.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pbl5.R

@Composable
fun LoginScreen(onBackClick: () -> Unit) {
    // Quản lý trạng thái cho số điện thoại
    val phoneNumber = remember { mutableStateOf("") }

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
                text = "Đăng nhập",
                fontSize = 24.sp,
                color = Color(0xFF333333),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Text(
                text = "Vui lòng nhập số điện thoại để nhận OTP và đăng nhập.",
                fontSize = 16.sp,
                color = Color(0xFF666666),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 40.dp)
                    .widthIn(max = 300.dp)
            )
            OutlinedTextField(
                value = phoneNumber.value, // Gán giá trị từ state
                onValueChange = { phoneNumber.value = it }, // Cập nhật state khi người dùng nhập
                label = { Text("Số điện thoại") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(8.dp)
            )
            Button(
                onClick = { /* TODO: Xử lý đăng nhập với phoneNumber.value */ },
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
                onClick = onBackClick,
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
                Text("Quay lại", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}