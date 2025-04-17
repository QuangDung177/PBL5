package com.example.pbl5.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsViewModel(
    private val mainViewModel: MainViewModel
) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val raspberryPiDocRef = db.collection("RASPBERRY_PIS").document("39f7b265cf615074")

    private val _deadFishThreshold = MutableStateFlow("1")
    val deadFishThreshold: StateFlow<String> = _deadFishThreshold.asStateFlow()

    private val _turbidityThreshold = MutableStateFlow("2.0")
    val turbidityThreshold: StateFlow<String> = _turbidityThreshold.asStateFlow()

    init {
        fetchSettingsFromFirebase()
    }

    private fun fetchSettingsFromFirebase() {
        raspberryPiDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val settings = document.get("settings") as? Map<String, Any>
                    val deadFishThresholdValue = settings?.get("deadFishThreshold")?.toString() ?: "1"
                    val turbidityThresholdValue = settings?.get("turbidityThreshold")?.toString() ?: "2.0"

                    _deadFishThreshold.value = deadFishThresholdValue
                    _turbidityThreshold.value = turbidityThresholdValue
                }
            }
            .addOnFailureListener { e ->
                println("Failed to fetch settings: $e")
            }
    }

    fun updateDeadFishThreshold(value: String) {
        _deadFishThreshold.value = value
    }

    fun updateTurbidityThreshold(value: String) {
        _turbidityThreshold.value = value
    }

    suspend fun saveThresholdsToFirebase(): Boolean {
        return try {
            val deadFishThresholdValue = _deadFishThreshold.value.toIntOrNull() ?: 1
            val turbidityThresholdValue = _turbidityThreshold.value.toDoubleOrNull() ?: 2.0

            val updates = hashMapOf(
                "settings.deadFishThreshold" to deadFishThresholdValue,
                "settings.turbidityThreshold" to turbidityThresholdValue
            )

            raspberryPiDocRef.update(updates as Map<String, Any>).await()
            println("Updated deadFishThreshold to $deadFishThresholdValue and turbidityThreshold to $turbidityThresholdValue")
            true // Thành công
        } catch (e: Exception) {
            println("Failed to update thresholds: $e")
            false // Thất bại
        }
    }
}