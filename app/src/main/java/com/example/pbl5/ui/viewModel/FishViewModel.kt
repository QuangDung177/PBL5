package com.example.pbl5.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pbl5.data.DeadFishData
import com.example.pbl5.data.DeadFishHistory
import com.example.pbl5.data.RaspberryPiData
import com.example.pbl5.data.RaspberryPiRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FishViewModel(
    private val repository: RaspberryPiRepository = RaspberryPiRepository(),
    private val mainViewModel: MainViewModel
) : ViewModel() {
    val raspberryPiData = mainViewModel.raspberryPiData
    val deadFishData = mainViewModel.deadFishData
    val serialId = mainViewModel.serialId

    val deadFishHistory = mutableStateOf<List<DeadFishHistory>>(emptyList())
    val filteredDeadFishHistory = mutableStateOf<List<DeadFishHistory>>(emptyList()) // Danh sách đã lọc
    val errorMessage = mutableStateOf<String?>(null)

    fun loadDeadFishHistory() {
        if (serialId.value.isEmpty()) {
            errorMessage.value = "Vui lòng kết nối với Raspberry Pi từ trang chính"
            filteredDeadFishHistory.value = emptyList()
            return
        }

        viewModelScope.launch {
            val history = repository.getDeadFishHistory(serialId.value)
            deadFishHistory.value = history
            filteredDeadFishHistory.value = history // Ban đầu hiển thị toàn bộ lịch sử
            if (history.isEmpty()) {
                errorMessage.value = "Không có lịch sử cá chết"
            }
            Log.d("FishViewModel", "Đã tải lịch sử cá chết: ${history.size} mục")
        }
    }

    fun filterDeadFishHistoryByDate(selectedDateMillis: Long) {
        val selectedDate = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nextDay = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }

        val filteredList = deadFishHistory.value.filter { history ->
            history.timestamp?.time?.let { timestamp ->
                timestamp >= selectedDate.timeInMillis && timestamp < nextDay.timeInMillis
            } ?: false
        }

        filteredDeadFishHistory.value = filteredList
        errorMessage.value = if (filteredList.isEmpty()) {
            "Không có lịch sử cá chết cho ngày ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time)}"
        } else {
            null
        }

        Log.d("FishViewModel", "Lọc theo ngày ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time)}: ${filteredList.size} mục")
    }
    fun resetFilter() {
        filteredDeadFishHistory.value = deadFishHistory.value
        errorMessage.value = if (deadFishHistory.value.isEmpty()) {
            "Không có lịch sử cá chết"
        } else {
            null
        }
        Log.d("FishViewModel", "Đã đặt lại bộ lọc: ${filteredDeadFishHistory.value.size} mục")
    }
}