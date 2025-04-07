package com.example.pbl5.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pbl5.data.*
import com.example.pbl5.data.RaspberryPiRepository
import com.example.pbl5.data.Result
import com.example.pbl5.data.UserData
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: RaspberryPiRepository = RaspberryPiRepository()
) : ViewModel() {
    val serialId = mutableStateOf("")
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    val raspberryPiData = mutableStateOf<RaspberryPiData?>(null)
    val deadFishData = mutableStateOf<DeadFishData?>(null)
    val turbidityData = mutableStateOf<TurbidityData?>(null)
    val turbidityDistribution = mutableStateOf(TurbidityDistribution())

    val userDisplayName = mutableStateOf("User")
    val userPhoneNumber = mutableStateOf("")

    init {
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
    }

    fun connectToRaspberryPi() {
        if (serialId.value.isEmpty()) {
            errorMessage.value = "Vui lòng nhập Serial ID"
            return
        }

        isLoading.value = true
        errorMessage.value = null

        viewModelScope.launch {
            val piData = repository.getRaspberryPiData(serialId.value)
            if (piData != null) {
                raspberryPiData.value = piData
                val deadFish = repository.getLatestDeadFishCount(serialId.value)
                deadFishData.value = deadFish
                println("DeadFishData updated: ${deadFish?.count}")
                turbidityData.value = repository.getLatestTurbidity(serialId.value)
                turbidityDistribution.value = repository.getTurbidityDistribution(serialId.value)
            } else {
                errorMessage.value = "Không tìm thấy Raspberry Pi với Serial ID: ${serialId.value}"
            }
            isLoading.value = false
        }
    }

    fun refreshData() {
        if (serialId.value.isNotEmpty()) {
            connectToRaspberryPi()
        }
    }
}