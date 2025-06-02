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
    serialId: String = "39f7b265cf615074"
) : ViewModel() {
    private val repository: RaspberryPiRepository = RaspberryPiRepository(context)

    val serialId = mutableStateOf(serialId)
    val isLoading = mutableStateOf(false)
    val isRefreshing = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val isConnected = mutableStateOf(false)

    val raspberryPiData = mutableStateOf<RaspberryPiData?>(null)
    val deadFishData = mutableStateOf<DeadFishData?>(null)
    val turbidityData = mutableStateOf<TurbidityData?>(null)
    val turbidityDistribution = mutableStateOf(TurbidityDistribution())
    val notifications = mutableStateOf<List<NotificationData>>(emptyList())

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
                    loadNotifications()
                }
                is Result.Error -> {
                    println("Failed to load user data: ${result.message}")
                    errorMessage.value = result.message
                }
            }
        }

        loadRaspberryPiData()
    }

    private fun loadRaspberryPiData() {
        isLoading.value = true
        errorMessage.value = null

        viewModelScope.launch {
            val piData = repository.getRaspberryPiData(serialId.value)
            if (piData != null) {
                raspberryPiData.value = piData
                deadFishData.value = repository.getLatestDeadFishCount(serialId.value)
                turbidityData.value = repository.getLatestTurbidity(serialId.value)
                turbidityDistribution.value = repository.getTurbidityDistribution(serialId.value)
                isConnected.value = true
            } else {
                errorMessage.value = "Không tìm thấy Raspberry Pi với Serial ID: ${serialId.value}"
                isConnected.value = false
            }
            isLoading.value = false
        }
    }

    fun refreshData() {
        if (isConnected.value && serialId.value.isNotBlank()) {
            isRefreshing.value = true
            viewModelScope.launch {
                val piData = repository.getRaspberryPiData(serialId.value)
                if (piData != null) {
                    raspberryPiData.value = piData
                    deadFishData.value = repository.getLatestDeadFishCount(serialId.value)
                    turbidityData.value = repository.getLatestTurbidity(serialId.value)
                    turbidityDistribution.value = repository.getTurbidityDistribution(serialId.value)
                    loadNotifications()
                } else {
                    println("Failed to refresh data for Serial ID: ${serialId.value}")
                }
                isRefreshing.value = false
            }
        }
    }

    fun connectToRaspberryPi() {
        loadRaspberryPiData()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            val phone = userPhoneNumber.value
            println("Loading notifications for userPhone: $phone")
            if (phone.isNotBlank()) {
                val fetchedNotifications = repository.getNotifications(phone)
                notifications.value = fetchedNotifications
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notificationId)
            loadNotifications()
        }
    }
}