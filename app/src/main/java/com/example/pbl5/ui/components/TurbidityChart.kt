package com.example.pbl5.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pbl5.data.TurbidityDistribution
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TurbidityChart(distribution: TurbidityDistribution) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // Đặt nền trắng
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Độ đục nước",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Biểu đồ tròn
                val total = (distribution.below2 + distribution.between2And3 + distribution.above3).toFloat()
                val below2Angle = if (total > 0) (distribution.below2 / total) * 360f else 0f
                val between2And3Angle = if (total > 0) (distribution.between2And3 / total) * 360f else 0f
                val above3Angle = if (total > 0) (distribution.above3 / total) * 360f else 0f

                Canvas(
                    modifier = Modifier
                        .size(120.dp)
                        .padding(16.dp)
                ) {
                    var startAngle = 0f
                    // Vẽ phần < 2.0 NTU (Xanh la)
                    drawArc(
                        color = Color.Green,
                        startAngle = startAngle,
                        sweepAngle = below2Angle,
                        useCenter = true,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height)
                    )
                    startAngle += below2Angle
                    // Vẽ phần 2.0 - 3.0 NTU (mau vang)
                    drawArc(
                        color = Color(0xFFFFA500),
                        startAngle = startAngle,
                        sweepAngle = between2And3Angle,
                        useCenter = true,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height)
                    )
                    startAngle += between2And3Angle
                    // Vẽ phần > 3.0 NTU (đỏ)
                    drawArc(
                        color = Color.Red,
                        startAngle = startAngle,
                        sweepAngle = above3Angle,
                        useCenter = true,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height)
                    )
                }

                // Chú thích
                Column {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color.Green)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tốt: <2.0 NTU (${distribution.below2})",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(0xFFFFA500))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Trung bình: 2.0 - 3.0 NTU (${distribution.between2And3})",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color.Red)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Xấu: >3.0 NTU (${distribution.above3})",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}