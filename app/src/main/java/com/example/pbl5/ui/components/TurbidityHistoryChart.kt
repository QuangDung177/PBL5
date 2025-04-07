package com.example.pbl5.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.toArgb
import com.example.pbl5.data.TurbidityHistory
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TurbidityHistoryChart(history: List<TurbidityHistory>) {
    if (history.isEmpty()) {
        Text(
            text = "Không có dữ liệu lịch sử độ đục nước",
            color = Color.Gray,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    // State để theo dõi vị trí bắt đầu của dữ liệu hiển thị
    var startIndex by remember { mutableStateOf(maxOf(0, history.size - 3)) }
    val itemsPerPage = 3 // Hiển thị 3 mục mỗi lần

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Lịch sử độ đục nước",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val sortedHistory = history.sortedBy { it.timestamp?.time ?: 0L }
            val displayHistory = sortedHistory.subList(
                startIndex,
                minOf(startIndex + itemsPerPage, sortedHistory.size)
            )

            if (displayHistory.size < 2) {
                Text(
                    text = "Dữ liệu không đủ để vẽ biểu đồ",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
                return@Column
            }

            // Tạo dữ liệu cho biểu đồ
            val entries = displayHistory.mapIndexed { index, entry ->
                entryOf(index.toFloat(), entry.value)
            }

            val chartEntryModelProducer = ChartEntryModelProducer(entries)

            // Vẽ biểu đồ
            Chart(
                chart = lineChart(listOf(
                    com.patrykandpatrick.vico.core.chart.line.LineChart.LineSpec(
                        lineColor = Color.Blue.toArgb() // Đổi màu thành xanh dương
                    )
                )),
                chartModelProducer = chartEntryModelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                startAxis = rememberStartAxis(
                    valueFormatter = AxisValueFormatter { value, _ ->
                        String.format("%.1f", value)
                    },
                    tickLength = 0.dp
                ),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = AxisValueFormatter { value, _ ->
                        val index = value.toInt()
                        if (index in displayHistory.indices) {
                            val time = displayHistory[index].timestamp?.time ?: 0L
                            val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                            val entryDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(time))
                            if (currentDate == entryDate) {
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))
                            } else {
                                SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(time))
                            }
                        } else {
                            ""
                        }
                    },
                    labelRotationDegrees = 45f,
                    tickLength = 0.dp
                )
            )

            // Nút điều hướng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        if (startIndex > 0) {
                            startIndex = maxOf(0, startIndex - itemsPerPage)
                        }
                    },
                    enabled = startIndex > 0,
                    modifier = Modifier.width(120.dp)
                ) {
                    Text("Trước")
                }

                Button(
                    onClick = {
                        if (startIndex + itemsPerPage < sortedHistory.size) {
                            startIndex = minOf(
                                startIndex + itemsPerPage,
                                sortedHistory.size - itemsPerPage
                            )
                        }
                    },
                    enabled = startIndex + itemsPerPage < sortedHistory.size,
                    modifier = Modifier.width(120.dp)
                ) {
                    Text("Tiếp")
                }
            }
        }
    }
}