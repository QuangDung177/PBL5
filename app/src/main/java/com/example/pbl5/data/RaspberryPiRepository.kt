package com.example.pbl5.data

import com.example.pbl5.utils.FirebaseManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

data class RaspberryPiData(
    val totalFishCount: Int = 0,
    val status: String = "Hoạt động", // Trạng thái hoạt động của thiết bị
    val lastSeen: Date? = null
)

data class DeadFishData(
    val count: Int = 0
)

data class TurbidityData(
    val value: Float = 0f,
    val timestamp: Date? = null,
    val status: String = "Tốt" // Trạng thái độ đục nước: Tốt, Trung bình, Xấu
)

data class TurbidityDistribution(
    val below2: Int = 0,  // Số bản ghi < 2.0 NTU
    val between2And3: Int = 0,  // Số bản ghi 2.0 - 3.0 NTU
    val above3: Int = 0  // Số bản ghi > 3.0 NTU
)

class RaspberryPiRepository(
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseManager: FirebaseManager = FirebaseManager()
) {
    // Lấy thông tin Raspberry Pi
    suspend fun getRaspberryPiData(serialId: String): RaspberryPiData? {
        return try {
            val document = firestore.collection("RASPBERRY_PIS")
                .document(serialId)
                .get()
                .await()
            if (document.exists()) {
                RaspberryPiData(
                    totalFishCount = document.getLong("totalFishCount")?.toInt() ?: 0,
                    status = document.getString("status") ?: "Hoạt động", // Trạng thái thiết bị
                    lastSeen = document.getTimestamp("lastSeen")?.toDate()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getLatestDeadFishCount(serialId: String): DeadFishData? {
        return try {
            val snapshot = firestore.collection("DEAD_FISH_DETECTIONS")
                .whereEqualTo("serial_id", serialId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            if (snapshot.documents.isNotEmpty()) {
                val doc = snapshot.documents.first()
                println("DeadFishData: ${doc.data}") // Thêm log để kiểm tra dữ liệu
                DeadFishData(
                    count = doc.getLong("count")?.toInt() ?: 0
                )
            } else {
                println("No dead fish data found for serialId: $serialId")
                null
            }
        } catch (e: Exception) {
            println("Error fetching dead fish data: $e")
            null
        }
    }

    // Lấy dữ liệu độ đục nước mới nhất
    suspend fun getLatestTurbidity(serialId: String): TurbidityData? {
        return try {
            val snapshot = firestore.collection("TURBIDITY")
                .whereEqualTo("serial_id", serialId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            if (snapshot.documents.isNotEmpty()) {
                val doc = snapshot.documents.first()
                val value = doc.getDouble("value")?.toFloat() ?: 0f
                val status = when {
                    value < 2.0 -> "Tốt"
                    value <= 3.0 -> "Trung bình"
                    else -> "Xấu"
                }
                println("Turbidity data: ${doc.data}") // Thêm log
                TurbidityData(
                    value = value,
                    timestamp = doc.getTimestamp("timestamp")?.toDate(),
                    status = status
                )
            } else {
                println("No turbidity data found for serialId: $serialId")
                null
            }
        } catch (e: Exception) {
            println("Error fetching turbidity data: $e")
            null
        }
    }

    // Lấy phân bố độ đục nước (cho biểu đồ)
    suspend fun getTurbidityDistribution(serialId: String): TurbidityDistribution {
        return try {
            val snapshot = firestore.collection("TURBIDITY")
                .whereEqualTo("serial_id", serialId)
                .get()
                .await()
            var below2 = 0
            var between2And3 = 0
            var above3 = 0
            snapshot.documents.forEach { doc ->
                val value = doc.getDouble("value")?.toFloat() ?: 0f
                when {
                    value < 2.0 -> below2++
                    value in 2.0..3.0 -> between2And3++
                    else -> above3++
                }
            }
            TurbidityDistribution(
                below2 = below2,
                between2And3 = between2And3,
                above3 = above3
            )
        } catch (e: Exception) {
            TurbidityDistribution()
        }
    }
}