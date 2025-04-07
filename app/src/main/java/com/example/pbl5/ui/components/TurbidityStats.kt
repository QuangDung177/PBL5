package com.example.pbl5.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TurbidityStats(
    value: Float,
    status: String, // Trạng thái độ đục nước: Tốt, Trung bình, Xấu
    timestamp: Long?,
    deviceStatus: String // Trạng thái thiết bị: Hoạt động, Lỗi
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // Đặt nền trắng
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cột 1: Cảm biến độ đục nước
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tiêu đề
                Box(
                    modifier = Modifier
                        .height(40.dp) // Chiều cao cố định cho tiêu đề
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cảm biến độ đục nước",
                        fontSize = 14.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 2 // Giới hạn 2 dòng để tránh tràn
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Giá trị
                Box(
                    modifier = Modifier
                        .height(40.dp) // Chiều cao cố định cho giá trị
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${value} NTU",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (status) {
                            "Tốt" -> Color.Green
                            "Trung bình" -> Color(0xFFFFA500)
                            else -> Color.Red
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Cột 2: Chỉ số
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chỉ số",
                        fontSize = 14.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = status,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (status) {
                            "Tốt" -> Color.Green
                            "Trung bình" -> Color(0xFFFFA500)
                            else -> Color.Red
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }

//            // Cột 3: Trạng thái thiết bị
//            Column(
//                modifier = Modifier
//                    .weight(1f)
//                    .padding(horizontal = 8.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Box(
//                    modifier = Modifier
//                        .height(40.dp)
//                        .fillMaxWidth(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = "Trạng thái thiết bị",
//                        fontSize = 14.sp,
//                        color = Color.Black,
//                        textAlign = TextAlign.Center,
//                        maxLines = 2
//                    )
//                }
//                Spacer(modifier = Modifier.height(4.dp))
//                Box(
//                    modifier = Modifier
//                        .height(40.dp)
//                        .fillMaxWidth(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = if (deviceStatus == "Active") "Active" else "Error",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = if (deviceStatus == "Active") Color.Green else Color.Red,
//                        textAlign = TextAlign.Center
//                    )
//                }
//            }

            // Cột 4: Thời gian
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Thời gian",
                        fontSize = 14.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = timestamp?.let {
                            SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(it)
                        } ?: "N/A",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}