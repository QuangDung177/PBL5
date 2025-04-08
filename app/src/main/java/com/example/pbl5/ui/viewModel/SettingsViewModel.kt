package com.example.pbl5.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val mainViewModel: MainViewModel // Nhận MainViewModel làm tham số
) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val raspberryPiDocRef = db.collection("RASPBERRY_PIS").document("39f7b265cf615074")

    // Trạng thái cho deadFishThreshold
    private val _deadFishThreshold = MutableStateFlow("1")
    val deadFishThreshold: StateFlow<String> = _deadFishThreshold.asStateFlow()

    // Trạng thái cho turbidityThreshold
    private val _turbidityThreshold = MutableStateFlow("2")
    val turbidityThreshold: StateFlow<String> = _turbidityThreshold.asStateFlow()

    // Khởi tạo: Lấy giá trị từ Firebase khi ViewModel được tạo
    init {
        fetchSettingsFromFirebase()
    }

    // Lấy dữ liệu từ Firebase
    private fun fetchSettingsFromFirebase() {
        raspberryPiDocRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val settings = document.get("settings") as? Map<String, Any>
                    val deadFishThresholdValue = settings?.get("deadFishThreshold")?.toString() ?: "1"
                    val turbidityThresholdValue = settings?.get("turbidityThreshold")?.toString() ?: "2"

                    _deadFishThreshold.value = deadFishThresholdValue
                    _turbidityThreshold.value = turbidityThresholdValue
                }
            }
            .addOnFailureListener { e ->
                println("Failed to fetch settings: $e")
            }
    }

    // Cập nhật deadFishThreshold (chỉ cập nhật trạng thái, không lưu Firebase ngay)
    fun updateDeadFishThreshold(value: String) {
        _deadFishThreshold.value = value
    }

    // Cập nhật turbidityThreshold (chỉ cập nhật trạng thái, không lưu Firebase ngay)
    fun updateTurbidityThreshold(value: String) {
        _turbidityThreshold.value = value
    }

    // Lưu cả hai giá trị lên Firebase khi được gọi
    fun saveThresholdsToFirebase() {
        viewModelScope.launch {
            val deadFishThresholdValue = _deadFishThreshold.value.toIntOrNull() ?: 1
            val turbidityThresholdValue = _turbidityThreshold.value.toIntOrNull() ?: 2

            // Cập nhật cả hai giá trị cùng lúc
            val updates = hashMapOf(
                "settings.deadFishThreshold" to deadFishThresholdValue,
                "settings.turbidityThreshold" to turbidityThresholdValue
            )

            raspberryPiDocRef.update(updates as Map<String, Any>)
                .addOnSuccessListener {
                    println("Updated deadFishThreshold to $deadFishThresholdValue and turbidityThreshold to $turbidityThresholdValue")
                }
                .addOnFailureListener { e ->
                    println("Failed to update thresholds: $e")
                }
        }
    }
}