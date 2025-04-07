package com.example.pbl5.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pbl5.data.DeadFishData
import com.example.pbl5.data.RaspberryPiData
import com.example.pbl5.data.RaspberryPiRepository
import com.example.pbl5.data.TurbidityData
import com.example.pbl5.data.TurbidityDistribution
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            println("Current user is null - No user is logged in")
        } else {
            println("Current user: ${currentUser.uid}, Phone: ${currentUser.phoneNumber}")
            // Bỏ tiền tố +84 và thêm số 0 vào đầu
            val rawPhoneNumber = currentUser.phoneNumber?.removePrefix("+84") ?: ""
            userPhoneNumber.value = if (rawPhoneNumber.isNotEmpty()) "0$rawPhoneNumber" else ""
            println("Processed phone number: ${userPhoneNumber.value}")
            viewModelScope.launch {
                try {
                    println("Fetching user data from Firestore with document ID: ${userPhoneNumber.value}")
                    val doc = repository.firestore.collection("USERS")
                        .document(userPhoneNumber.value)
                        .get()
                        .await()
                    if (doc.exists()) {
                        val displayName = doc.getString("displayName") ?: "User"
                        println("User document found - DisplayName: $displayName")
                        userDisplayName.value = displayName
                    } else {
                        println("User document does not exist for phone number: ${userPhoneNumber.value}")
                    }
                } catch (e: Exception) {
                    println("Error fetching user data: $e")
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
                println("DeadFishData updated: ${deadFish?.count}") // Thêm log
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