package com.example.pbl5.data

import com.example.pbl5.utils.FirebaseManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Date
import java.util.concurrent.TimeUnit

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
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Thay bằng FCM Server Key của bạn (lấy từ Firebase Console > Project Settings > Cloud Messaging)
    private val FCM_SERVER_KEY = "YOUR_FCM_SERVER_KEY_HERE"

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
            var below100 = 0
            var between100And300 = 0
            var above300 = 0
            snapshot.documents.forEach { doc ->
                val value = doc.getDouble("value")?.toFloat() ?: 0f
                when {
                    value < 100.0 -> below100++
                    value in 100.0..300.0 -> between100And300++
                    else -> above300++
                }
            }
            TurbidityDistribution(
                below2 = below100,
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

    // Lấy ngưỡng từ Firestore
    suspend fun getThresholds(serialId: String): Pair<Int, Float>? {
        return try {
            val document = firestore.collection("RASPBERRY_PIS")
                .document(serialId)
                .get()
                .await()
            if (document.exists()) {
                val deadFishThreshold = document.getLong("deadFishThreshold")?.toInt() ?: 0
                val turbidityThreshold = document.getDouble("turbidityThreshold")?.toFloat() ?: 0f
                Pair(deadFishThreshold, turbidityThreshold)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error fetching thresholds: ${e.message}")
            null
        }
    }

    // Lấy FCM token của người dùng dựa trên ownerPhone
    suspend fun getFcmToken(phoneNumber: String): String? {
        return try {
            val document = firestore.collection("USERS")
                .document(phoneNumber)
                .get()
                .await()
            if (document.exists()) {
                document.getString("fcmToken")
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error fetching FCM token: ${e.message}")
            null
        }
    }

    // Gửi thông báo qua FCM
    suspend fun sendNotification(toToken: String, title: String, body: String): Boolean {
        return try {
            val json = JSONObject().apply {
                put("to", toToken)
                put("notification", JSONObject().apply {
                    put("title", title)
                    put("body", body)
                })
            }

            val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .addHeader("Authorization", "key=$FCM_SERVER_KEY")
                .build()

            val response = client.newCall(request).execute()
            val success = response.isSuccessful
            response.close()
            success
        } catch (e: Exception) {
            println("Error sending FCM notification: ${e.message}")
            false
        }
    }

    // Kiểm tra ngưỡng và gửi thông báo nếu vượt ngưỡng
    suspend fun checkAndNotify(serialId: String) {
        // Lấy ngưỡng
        val thresholds = getThresholds(serialId) ?: return
        val (deadFishThreshold, turbidityThreshold) = thresholds

        // Lấy ownerPhone từ RASPBERRY_PIS
        val raspberryPiDoc = firestore.collection("RASPBERRY_PIS")
            .document(serialId)
            .get()
            .await()
        if (!raspberryPiDoc.exists()) return
        val ownerPhone = raspberryPiDoc.getString("ownerPhone") ?: return

        // Lấy FCM token của người dùng
        val fcmToken = getFcmToken(ownerPhone) ?: return

        // Kiểm tra số cá chết
        val deadFishData = getLatestDeadFishCount(serialId)
        if (deadFishData != null && deadFishData.count > deadFishThreshold) {
            val message = "Raspberry Pi detected ${deadFishData.count} dead fish, exceeding threshold of $deadFishThreshold!"
            sendNotification(fcmToken, "Dead Fish Alert", message)

            // Lưu thông báo vào Firestore
            val notificationData = hashMapOf(
                "message" to message,
                "timestamp" to Date(),
                "userPhone" to ownerPhone
            )
            firestore.collection("NOTIFICATIONS").add(notificationData).await()
        }

        // Kiểm tra độ đục nước
        val turbidityData = getLatestTurbidity(serialId)
        if (turbidityData != null && turbidityData.value > turbidityThreshold) {
            val message = "Water turbidity is ${turbidityData.value}, exceeding threshold of $turbidityThreshold!"
            sendNotification(fcmToken, "Turbidity Alert", message)

            // Lưu thông báo vào Firestore
            val notificationData = hashMapOf(
                "message" to message,
                "timestamp" to Date(),
                "userPhone" to ownerPhone
            )
            firestore.collection("NOTIFICATIONS").add(notificationData).await()
        }
    }
}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}