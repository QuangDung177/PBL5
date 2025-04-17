package com.example.pbl5
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseMsgService"
    private val firestore = FirebaseFirestore.getInstance()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Kiểm tra nếu thông báo có notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            showNotification(it.title, it.body)
        }

        // Kiểm tra nếu thông báo có data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            // Xử lý data payload nếu cần
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Lưu token mới vào Firestore
        saveFcmToken(token)
    }

    private fun saveFcmToken(token: String) {
        // Lấy số điện thoại người dùng (dựa trên Firebase Auth)
        val userPhone = getUserPhoneNumber()
        if (userPhone.isEmpty()) {
            Log.e(TAG, "Không thể lấy số điện thoại để lưu FCM token")
            return
        }

        // Lưu token vào Firestore
        firestore.collection("USERS")
            .document(userPhone)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "Cập nhật FCM token thành công cho user: $userPhone")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Lỗi khi cập nhật FCM token: ${e.message}")
            }
    }

    private fun getUserPhoneNumber(): String {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val rawPhoneNumber = currentUser?.phoneNumber?.removePrefix("+84") ?: ""
        return if (rawPhoneNumber.isNotEmpty()) "0$rawPhoneNumber" else ""
    }

    private fun showNotification(title: String?, message: String?) {
        val channelId = "default_channel_id"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Tạo Notification Channel cho Android 8.0 (API 26) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Tạo thông báo
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Thay bằng icon của bạn
            .setContentTitle(title ?: "Notification")
            .setContentText(message)
            .setAutoCancel(true)

        // Hiển thị thông báo
        notificationManager.notify(0, notificationBuilder.build())
    }
}