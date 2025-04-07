package com.example.pbl5.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
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
            val minTime = sortedHistory.firstOrNull()?.timestamp?.time ?: 0L
            val maxTime = sortedHistory.lastOrNull()?.timestamp?.time ?: 0L

            if (minTime == maxTime) {
                Text(
                    text = "Dữ liệu không đủ để vẽ biểu đồ",
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
                return@Column
            }

            // Thêm log để kiểm tra dữ liệu
            println("TurbidityHistory in TurbidityHistoryChart: $sortedHistory")
            println("minTime: $minTime, maxTime: $maxTime")
            println("Values: ${sortedHistory.map { it.value }}")

            // Tạo dữ liệu cho biểu đồ
            val entries = sortedHistory.mapIndexed { index, entry ->
                entryOf(index.toFloat(), entry.value)
            }

            val chartEntryModelProducer = ChartEntryModelProducer(entries)

            // Vẽ biểu đồ bằng LineChart của vico
            Chart(
                chart = lineChart(),
                chartModelProducer = chartEntryModelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp), // Tăng chiều cao để có không gian cho nhãn
                startAxis = rememberStartAxis(
                    valueFormatter = AxisValueFormatter { value, _ ->
                        String.format("%.1f", value)
                    },
                    tickLength = 0.dp // Tắt tick để giống hình ảnh trước đó
                ),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = AxisValueFormatter { value, _ ->
                        val index = value.toInt()
                        if (index in sortedHistory.indices) {
                            val time = sortedHistory[index].timestamp?.time ?: 0L
                            // So sánh với ngày hiện tại
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
                    labelRotationDegrees = 45f, // Xoay nhãn 45 độ để tránh chồng lấn
                    tickLength = 0.dp // Tắt tick để giống hình ảnh trước đó
                )
            )
        }
    }
}