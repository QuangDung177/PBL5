package com.example.pbl5.ui.components

import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color as AndroidColor
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pbl5.data.TurbidityHistory
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun TurbidityHistoryChart(history: List<TurbidityHistory>) {
    Log.d("TurbidityHistoryChart", "Số lượng dữ liệu history: ${history.size}")
    history.forEachIndexed { index, entry ->
        Log.d("TurbidityHistoryChart", "Entry $index: value=${entry.value}, timestamp=${entry.timestamp?.time}")
    }

    if (history.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = Color.White,
            elevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không có dữ liệu lịch sử độ đục nước",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        }
        return
    }

    var startIndex by remember { mutableStateOf(maxOf(0, history.size - 3)) }
    val itemsPerPage = 3

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color.White,
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Tiêu đề với icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ShowChart,
                    contentDescription = "Turbidity History",
                    tint = Color(0xFF1E90FF),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lịch sử độ đục nước",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            val sortedHistory = history.sortedBy { it.timestamp?.time ?: 0L }
            val displayHistory = sortedHistory.subList(
                startIndex,
                minOf(startIndex + itemsPerPage, sortedHistory.size)
            )

            if (displayHistory.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Dữ liệu không đủ để vẽ biểu đồ",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    factory = { context ->
                        LineChart(context).apply {
                            description.isEnabled = false
                            setTouchEnabled(true)
                            isDragEnabled = true
                            setScaleEnabled(false)
                            setPinchZoom(false)
                            setDrawGridBackground(false)

                            // Tùy chỉnh trục X
                            xAxis.apply {
                                position = XAxis.XAxisPosition.BOTTOM
                                setDrawGridLines(false)
                                textColor = AndroidColor.BLACK
                                textSize = 10f
                                granularity = 1f
                                labelRotationAngle = 45f
                                valueFormatter = IndexAxisValueFormatter(
                                    displayHistory.map { entry ->
                                        val time = entry.timestamp?.time ?: 0L
                                        if (time == 0L) "Invalid"
                                        else try {
                                            val date = Date(time)
                                            val timePart = SimpleDateFormat("HH:mm", Locale("vi", "VN")).format(date)
                                            val datePart = SimpleDateFormat("dd/MM", Locale("vi", "VN")).format(date)
                                            "$timePart\n$datePart"
                                        } catch (e: Exception) {
                                            "Error"
                                        }
                                    }
                                )
                            }

                            // Tùy chỉnh trục Y
                            axisLeft.apply {
                                textColor = AndroidColor.BLACK
                                textSize = 12f
                                setDrawGridLines(true)
                                axisMinimum = 0f
                                gridColor = AndroidColor.LTGRAY
                                gridLineWidth = 0.5f
                            }
                            axisRight.isEnabled = false

                            // Tạo dữ liệu biểu đồ
                            val entries = displayHistory.mapIndexed { index, entry ->
                                Entry(index.toFloat(), if (entry.value >= 0) entry.value else 0f)
                            }
                            val dataSet = LineDataSet(entries, "Độ đục (NTU)").apply {
                                color = AndroidColor.parseColor("#1E90FF")
                                lineWidth = 2f
                                setDrawCircles(true)
                                circleRadius = 4f
                                circleColors = listOf(AndroidColor.parseColor("#1E90FF"))
                                setDrawValues(false)
                                mode = LineDataSet.Mode.CUBIC_BEZIER
                                setDrawFilled(true)
                                fillColor = AndroidColor.parseColor("#1E90FF33")
                            }
                            data = LineData(dataSet)

                            // Thêm animation
                            animateX(1000)

                            invalidate()
                        }
                    },
                    update = { chart ->
                        val entries = displayHistory.mapIndexed { index, entry ->
                            Entry(index.toFloat(), if (entry.value >= 0) entry.value else 0f)
                        }
                        val dataSet = LineDataSet(entries, "Độ đục (NTU)").apply {
                            color = AndroidColor.parseColor("#1E90FF")
                            lineWidth = 2f
                            setDrawCircles(true)
                            circleRadius = 4f
                            circleColors = listOf(AndroidColor.parseColor("#1E90FF"))
                            setDrawValues(false)
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                            setDrawFilled(true)
                            fillColor = AndroidColor.parseColor("#1E90FF33")
                        }
                        chart.data = LineData(dataSet)
                        chart.xAxis.apply {
                            valueFormatter = IndexAxisValueFormatter(
                                displayHistory.map { entry ->
                                    val time = entry.timestamp?.time ?: 0L
                                    if (time == 0L) "Invalid"
                                    else try {
                                        val date = Date(time)
                                        val timePart = SimpleDateFormat("HH:mm", Locale("vi", "VN")).format(date)
                                        val datePart = SimpleDateFormat("dd/MM", Locale("vi", "VN")).format(date)
                                        "$timePart\n$datePart"
                                    } catch (e: Exception) {
                                        "Error"
                                    }
                                }
                            )
                            textSize = 10f
                            labelRotationAngle = 45f
                        }
                        chart.animateX(500)
                        chart.invalidate()
                    }
                )
            }

            // Nút điều hướng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        if (startIndex > 0) {
                            startIndex = maxOf(0, startIndex - itemsPerPage)
                        }
                    },
                    enabled = startIndex > 0,
                    modifier = Modifier.width(120.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF1E90FF),
                        disabledContentColor = Color.Gray
                    ),
                    border = BorderStroke(1.dp, if (startIndex > 0) Color(0xFF1E90FF) else Color.Gray)
                ) {
                    Text("Trước")
                }

                OutlinedButton(
                    onClick = {
                        if (startIndex + itemsPerPage < sortedHistory.size) {
                            startIndex = minOf(
                                startIndex + itemsPerPage,
                                sortedHistory.size - itemsPerPage
                            )
                        }
                    },
                    enabled = startIndex + itemsPerPage < sortedHistory.size,
                    modifier = Modifier.width(120.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF1E90FF),
                        disabledContentColor = Color.Gray
                    ),
                    border = BorderStroke(1.dp, if (startIndex + itemsPerPage < sortedHistory.size) Color(0xFF1E90FF) else Color.Gray)
                ) {
                    Text("Tiếp")
                }
            }
        }
    }
}