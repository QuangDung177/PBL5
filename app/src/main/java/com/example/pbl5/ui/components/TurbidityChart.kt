package com.example.pbl5.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pbl5.data.TurbidityDistribution

@Composable
fun TurbidityChart(distribution: TurbidityDistribution) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color.White,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Card title with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.BarChart,
                    contentDescription = "Turbidity Distribution",
                    tint = Color(0xFF1E90FF),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Phân bố độ đục nước",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Chart and legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pie chart with enhanced visuals
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val total = (distribution.below2 + distribution.between2And3 + distribution.above3).toFloat()
                    val below2Angle = if (total > 0) (distribution.below2 / total) * 360f else 0f
                    val between2And3Angle = if (total > 0) (distribution.between2And3 / total) * 360f else 0f
                    val above3Angle = if (total > 0) (distribution.above3 / total) * 360f else 0f

                    Canvas(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(percent = 50))
                    ) {
                        var startAngle = 0f

                        // Draw segments with slight padding between them
                        if (below2Angle > 0) {
                            drawArc(
                                color = Color.Green,
                                startAngle = startAngle,
                                sweepAngle = below2Angle - 1f,
                                useCenter = true,
                                topLeft = Offset(size.width * 0.05f, size.height * 0.05f),
                                size = Size(size.width * 0.9f, size.height * 0.9f)
                            )
                            startAngle += below2Angle
                        }

                        if (between2And3Angle > 0) {
                            drawArc(
                                color = Color(0xFFFFA500), // Orange
                                startAngle = startAngle,
                                sweepAngle = between2And3Angle - 1f,
                                useCenter = true,
                                topLeft = Offset(size.width * 0.05f, size.height * 0.05f),
                                size = Size(size.width * 0.9f, size.height * 0.9f)
                            )
                            startAngle += between2And3Angle
                        }

                        if (above3Angle > 0) {
                            drawArc(
                                color = Color.Red,
                                startAngle = startAngle,
                                sweepAngle = above3Angle - 1f,
                                useCenter = true,
                                topLeft = Offset(size.width * 0.05f, size.height * 0.05f),
                                size = Size(size.width * 0.9f, size.height * 0.9f)
                            )
                        }

                        // Draw a small white circle in the center for a donut chart effect
                        drawCircle(
                            color = Color.White,
                            radius = size.width * 0.3f,
                            center = Offset(size.width / 2, size.height / 2)
                        )
                    }

                    // Show total count in the center
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${total.toInt()}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Tổng",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Legend with enhanced styling
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    LegendItem(
                        color = Color.Green,
                        label = "Tốt: <100 NTU",
                        count = distribution.below2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LegendItem(
                        color = Color(0xFFFFA500), // Orange
                        label = "Trung bình: 100-300 NTU",
                        count = distribution.between2And3
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LegendItem(
                        color = Color.Red,
                        label = "Xấu: >300 NTU",
                        count = distribution.above3
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    count: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color indicator with rounded corners
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Label and count
        Column {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Black
            )
            Text(
                text = "$count mẫu",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}