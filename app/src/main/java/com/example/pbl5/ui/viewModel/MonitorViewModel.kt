package com.example.pbl5.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pbl5.data.RaspberryPiRepository
import com.example.pbl5.data.TurbidityData
import com.example.pbl5.data.TurbidityDistribution
import com.example.pbl5.data.TurbidityHistory
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MonitorViewModel(
    private val mainViewModel: MainViewModel,
    context: Context
) : ViewModel() {
    private val repository: RaspberryPiRepository = RaspberryPiRepository(context)

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _turbidityData = mutableStateOf<TurbidityData?>(null)
    val turbidityData: State<TurbidityData?> = _turbidityData

    private val _turbidityDistribution = mutableStateOf(TurbidityDistribution())
    val turbidityDistribution: State<TurbidityDistribution> = _turbidityDistribution

    private val _turbidityHistory = mutableStateOf<List<TurbidityHistory>>(emptyList())
    val turbidityHistory: State<List<TurbidityHistory>> = _turbidityHistory

    init {
        loadData()
    }

    private fun loadData() {
        val serialId = mainViewModel.serialId.value // Sửa: Sử dụng serialId.value
        if (serialId.isBlank()) {
            _errorMessage.value = "Serial ID không được để trống"
            return
        }
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                coroutineScope {
                    val turbidityJob = async {
                        try {
                            repository.getLatestTurbidity(serialId)
                        } catch (e: Exception) {
                            println("Error fetching TurbidityData: $e")
                            null
                        }
                    }
                    val distributionJob = async {
                        try {
                            repository.getTurbidityDistribution(serialId)
                        } catch (e: Exception) {
                            println("Error fetching TurbidityDistribution: $e")
                            TurbidityDistribution()
                        }
                    }
                    val historyJob = async {
                        try {
                            repository.getTurbidityHistory(serialId)
                        } catch (e: Exception) {
                            println("Error fetching TurbidityHistory: $e")
                            emptyList()
                        }
                    }

                    _turbidityData.value = turbidityJob.await()
                    _turbidityDistribution.value = distributionJob.await()
                    _turbidityHistory.value = historyJob.await()
                }
                if (_turbidityData.value == null) {
                    _errorMessage.value = "Không tìm thấy dữ liệu độ đục nước"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi tải dữ liệu: ${e.message}"
                println("Error loading data: $e")
            } finally {
                _isLoading.value = false
            }
        }
    }
}