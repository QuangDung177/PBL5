package com.example.pbl5.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pbl5.data.*
import com.example.pbl5.data.RaspberryPiRepository
import com.example.pbl5.data.Result
import com.example.pbl5.data.UserData
import kotlinx.coroutines.launch

class MainViewModel(
    context: Context,
    serialId: String = "39f7b265cf615074" // Giá trị mặc định
) : ViewModel() {
    private val repository: RaspberryPiRepository = RaspberryPiRepository(context)

    // Biến serialId thành MutableState để có thể thay đổi từ UI
    val serialId = mutableStateOf(serialId)

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    val raspberryPiData = mutableStateOf<RaspberryPiData?>(null)
    val deadFishData = mutableStateOf<DeadFishData?>(null)
    val turbidityData = mutableStateOf<TurbidityData?>(null)
    val turbidityDistribution = mutableStateOf(TurbidityDistribution())

    val userDisplayName = mutableStateOf("User")
    val userPhoneNumber = mutableStateOf("")

    init {
        // Load user data
        viewModelScope.launch {
            when (val result = repository.getUserData()) {
                is Result.Success -> {
                    val userData: UserData = result.data
                    userPhoneNumber.value = userData.phoneNumber
                    userDisplayName.value = userData.displayName
                    println("User data loaded successfully - Phone: ${userPhoneNumber.value}, DisplayName: ${userDisplayName.value}")
                }
                is Result.Error -> {
                    println("Failed to load user data: ${result.message}")
                    errorMessage.value = result.message
                }
            }
        }

        // Load Raspberry Pi data and check thresholds
        loadRaspberryPiData()
    }

    private fun loadRaspberryPiData() {
        isLoading.value = true
        errorMessage.value = null

        viewModelScope.launch {
            val piData = repository.getRaspberryPiData(serialId.value) // Sử dụng serialId.value
            if (piData != null) {
                raspberryPiData.value = piData
                val deadFish = repository.getLatestDeadFishCount(serialId.value)
                deadFishData.value = deadFish
                println("DeadFishData updated: ${deadFish?.count}")
                turbidityData.value = repository.getLatestTurbidity(serialId.value)
                turbidityDistribution.value = repository.getTurbidityDistribution(serialId.value)
                // Kiểm tra ngưỡng và gửi thông báo
                checkAndNotify()
            } else {
                errorMessage.value = "Không tìm thấy Raspberry Pi với Serial ID: ${serialId.value}"
            }
            isLoading.value = false
        }
    }

    fun refreshData() {
        loadRaspberryPiData()
    }

    // Hàm để xử lý khi người dùng nhấn nút kết nối
    fun connectToRaspberryPi() {
        loadRaspberryPiData() // Gọi lại loadRaspberryPiData với serialId mới
    }

    private fun checkAndNotify() {
        viewModelScope.launch {
            try {
                repository.checkAndNotify(serialId.value)
                println("Kiểm tra ngưỡng thành công cho serialId: ${serialId.value}")
            } catch (e: Exception) {
                println("Lỗi khi kiểm tra ngưỡng: ${e.message}")
                errorMessage.value = "Lỗi khi kiểm tra ngưỡng: ${e.message}"
            }
        }
    }
}