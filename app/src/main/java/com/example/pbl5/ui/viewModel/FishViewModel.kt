package com.example.pbl5.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
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
    context: Context,
    private val mainViewModel: MainViewModel
) : ViewModel() {
    private val repository: RaspberryPiRepository = RaspberryPiRepository(context)

    val raspberryPiData = mainViewModel.raspberryPiData
    val deadFishData = mainViewModel.deadFishData
    val serialId = mainViewModel.serialId.value

    private val _deadFishHistory = mutableStateOf<List<DeadFishHistory>>(emptyList())
    val deadFishHistory: State<List<DeadFishHistory>> = _deadFishHistory

    private val _filteredDeadFishHistory = mutableStateOf<List<DeadFishHistory>>(emptyList())
    val filteredDeadFishHistory: State<List<DeadFishHistory>> = _filteredDeadFishHistory

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadDeadFishHistory()
    }

    fun loadDeadFishHistory() {
        println("SerialId in FishViewModel: $serialId")
        if (serialId.isEmpty()) {
            _errorMessage.value = "Vui lòng kết nối với Raspberry Pi từ trang chính"
            _filteredDeadFishHistory.value = emptyList()
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val history = repository.getDeadFishHistory(serialId)
            _deadFishHistory.value = history
            _filteredDeadFishHistory.value = history
            if (history.isEmpty()) {
                _errorMessage.value = "Không có lịch sử cá chết"
            }
            Log.d("FishViewModel", "Đã tải lịch sử cá chết: ${history.size} mục")
            _isLoading.value = false
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

        val filteredList = _deadFishHistory.value.filter { history ->
            history.timestamp?.time?.let { timestamp ->
                timestamp >= selectedDate.timeInMillis && timestamp < nextDay.timeInMillis
            } ?: false
        }

        _filteredDeadFishHistory.value = filteredList
        _errorMessage.value = if (filteredList.isEmpty()) {
            "Không có lịch sử cá chết cho ngày ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time)}"
        } else {
            null
        }

        Log.d("FishViewModel", "Lọc theo ngày ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time)}: ${filteredList.size} mục")
    }

    fun resetFilter() {
        _filteredDeadFishHistory.value = _deadFishHistory.value
        _errorMessage.value = if (_deadFishHistory.value.isEmpty()) {
            "Không có lịch sử cá chết"
        } else {
            null
        }
        Log.d("FishViewModel", "Đã đặt lại bộ lọc: ${_filteredDeadFishHistory.value.size} mục")
    }
}