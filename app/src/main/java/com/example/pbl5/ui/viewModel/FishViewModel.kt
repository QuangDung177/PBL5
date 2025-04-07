package com.example.pbl5.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pbl5.data.DeadFishData
import com.example.pbl5.data.DeadFishHistory
import com.example.pbl5.data.RaspberryPiData
import com.example.pbl5.data.RaspberryPiRepository
import kotlinx.coroutines.launch

class FishViewModel(
    private val repository: RaspberryPiRepository = RaspberryPiRepository(),
    private val mainViewModel: MainViewModel
) : ViewModel() {
    val raspberryPiData = mainViewModel.raspberryPiData
    val deadFishData = mainViewModel.deadFishData
    val serialId = mainViewModel.serialId

    val deadFishHistory = mutableStateOf<List<DeadFishHistory>>(emptyList())
    val errorMessage = mutableStateOf<String?>(null)

    fun loadDeadFishHistory() {
        if (serialId.value.isEmpty()) {
            errorMessage.value = "Vui lòng kết nối với Raspberry Pi từ trang chính"
            return
        }

        viewModelScope.launch {
            val history = repository.getDeadFishHistory(serialId.value)
            deadFishHistory.value = history
            if (history.isEmpty()) {
                errorMessage.value = "Không có lịch sử cá chết"
            }
        }
    }
}