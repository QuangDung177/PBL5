package com.example.pbl5.ui.components

import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.toArgb
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
        Text(
            text = "Không có dữ liệu lịch sử độ đục nước",
            color = Color.Gray,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    var startIndex by remember { mutableStateOf(maxOf(0, history.size - 3)) }
    val itemsPerPage = 3

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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

                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            textColor = android.graphics.Color.BLACK
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

                        axisLeft.apply {
                            textColor = android.graphics.Color.BLACK
                            textSize = 12f
                            setDrawGridLines(true)
                            axisMinimum = 0f
                        }
                        axisRight.isEnabled = false

                        val entries = displayHistory.mapIndexed { index, entry ->
                            Entry(index.toFloat(), if (entry.value >= 0) entry.value else 0f)
                        }
                        val dataSet = LineDataSet(entries, "Độ đục (NTU)").apply {
                            color = Color.Blue.toArgb()
                            lineWidth = 2f
                            setDrawCircles(true)
                            circleRadius = 4f
                            circleColors = listOf(Color.Blue.toArgb())
                            setDrawValues(false)
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                        }
                        data = LineData(dataSet)
                        invalidate()
                    }
                },
                update = { chart ->
                    val entries = displayHistory.mapIndexed { index, entry ->
                        Entry(index.toFloat(), if (entry.value >= 0) entry.value else 0f)
                    }
                    val dataSet = LineDataSet(entries, "Độ đục (NTU)").apply {
                        color = Color.Blue.toArgb()
                        lineWidth = 2f
                        setDrawCircles(true)
                        circleRadius = 4f
                        circleColors = listOf(Color.Blue.toArgb())
                        setDrawValues(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
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
                    chart.invalidate()
                }
            )

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
