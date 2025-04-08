package com.example.pbl5.data

import com.example.pbl5.utils.FirebaseManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date

data class RaspberryPiData(
    val totalFishCount: Int = 0,
    val status: String = "Hoạt động",
    val lastSeen: Date? = null
)

data class DeadFishData(
    val count: Int = 0
)

data class TurbidityData(
    val value: Float = 0f,
    val timestamp: Date? = null,
    val status: String = "Tốt"
)

data class TurbidityDistribution(
    val below2: Int = 0,
    val between2And3: Int = 0,
    val above3: Int = 0
)

data class UserData(
    val phoneNumber: String = "",
    val displayName: String = "User"
)

data class DeadFishHistory(
    val id: String = "",
    val count: Int = 0,
    val timestamp: Date? = null,
    val imageUrl: String? = null
)
data class TurbidityHistory(
    val id: String = "",
    val value: Float = 0f,
    val timestamp: Date? = null
)
class RaspberryPiRepository(
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseManager: FirebaseManager = FirebaseManager()
) {
    // Lấy thông tin người dùng từ Firebase Auth và Firestore
    suspend fun getUserData(): Result<UserData> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Result.Error("Không có người dùng đăng nhập")
            } else {
                val rawPhoneNumber = currentUser.phoneNumber?.removePrefix("+84") ?: ""
                val phoneNumber = if (rawPhoneNumber.isNotEmpty()) "0$rawPhoneNumber" else ""
                if (phoneNumber.isEmpty()) {
                    Result.Error("Không thể lấy số điện thoại của người dùng")
                } else {
                    val doc = firestore.collection("USERS")
                        .document(phoneNumber)
                        .get()
                        .await()
                    if (doc.exists()) {
                        val displayName = doc.getString("displayName") ?: "User"
                        Result.Success(UserData(phoneNumber = phoneNumber, displayName = displayName))
                    } else {
                        Result.Error("Không tìm thấy thông tin người dùng với số điện thoại: $phoneNumber")
                    }
                }
            }
        } catch (e: Exception) {
            Result.Error("Lỗi khi lấy thông tin người dùng: ${e.message}")
        }
    }

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
                    status = document.getString("status") ?: "Hoạt động",
                    lastSeen = document.getTimestamp("lastSeen")?.toDate()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Lấy số cá chết mới nhất
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
                DeadFishData(
                    count = doc.getLong("count")?.toInt() ?: 0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Lấy lịch sử cá chết
    suspend fun getDeadFishHistory(serialId: String): List<DeadFishHistory> {
        return try {
            println("Fetching dead fish history for serialId: $serialId")
            val snapshot = firestore.collection("DEAD_FISH_DETECTIONS")
                .whereEqualTo("serial_id", serialId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val history = snapshot.documents.map { doc ->
                DeadFishHistory(
                    id = doc.id,
                    count = doc.getLong("count")?.toInt() ?: 0,
                    timestamp = doc.getTimestamp("timestamp")?.toDate(),
                    imageUrl = doc.getString("imageURL")
                )
            }
            println("Fetched ${history.size} dead fish history records")
            history
        } catch (e: Exception) {
            println("Error fetching DeadFishHistory: ${e.message}")
            emptyList()
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
                    value < 100.0 -> "Tốt"
                    value <= 300.0 -> "Trung bình"
                    else -> "Xấu"
                }
                TurbidityData(
                    value = value,
                    timestamp = doc.getTimestamp("timestamp")?.toDate(),
                    status = status
                )
            } else {
                null
            }
        } catch (e: Exception) {
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
            var below100 = 0  // Thay vì below2
            var between100And300 = 0  // Thay vì between2And3
            var above300 = 0  // Thay vì above3
            snapshot.documents.forEach { doc ->
                val value = doc.getDouble("value")?.toFloat() ?: 0f
                when {
                    value < 100.0 -> below100++
                    value in 100.0..300.0 -> between100And300++
                    else -> above300++
                }
            }
            TurbidityDistribution(
                below2 = below100,           // Giữ tên thuộc tính nhưng cập nhật giá trị
                between2And3 = between100And300,
                above3 = above300
            )
        } catch (e: Exception) {
            TurbidityDistribution()
        }
    }
    // Lấy lịch sử độ đục nước
    suspend fun getTurbidityHistory(serialId: String): List<TurbidityHistory> {
        return try {
            println("Fetching turbidity history for serialId: $serialId")
            val snapshot = firestore.collection("TURBIDITY")
                .whereEqualTo("serial_id", serialId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val history = snapshot.documents.map { doc ->
                TurbidityHistory(
                    id = doc.id,
                    value = doc.getDouble("value")?.toFloat() ?: 0f,
                    timestamp = doc.getTimestamp("timestamp")?.toDate()
                )
            }
            println("Fetched ${history.size} turbidity history records")
            history
        } catch (e: Exception) {
            println("Error fetching TurbidityHistory: ${e.message}")
            emptyList()
        }
    }
}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}