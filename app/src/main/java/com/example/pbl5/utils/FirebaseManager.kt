package com.example.pbl5.utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // Kiểm tra số điện thoại đã tồn tại trong Firestore (dùng cho đăng nhập)
    fun checkPhoneNumberForLogin(
        phoneNumber: String,
        onSuccess: () -> Unit,
        onNotFound: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("USERS").document(phoneNumber).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onSuccess()
                } else {
                    onNotFound()
                }
            }
            .addOnFailureListener { e ->
                onFailure("Lỗi kiểm tra số điện thoại: ${e.message}")
            }
    }

    // Kiểm tra số điện thoại đã tồn tại trong Firestore (dùng cho đăng ký)
    fun checkPhoneNumberForRegister(
        phoneNumber: String,
        onNotExists: () -> Unit,
        onExists: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("USERS").document(phoneNumber).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onNotExists()
                } else {
                    onExists()
                }
            }
            .addOnFailureListener { e ->
                onFailure("Lỗi kiểm tra số điện thoại: ${e.message}")
            }
    }

    // Xác minh OTP (dùng chung cho cả đăng nhập và đăng ký)
    fun verifyOtp(
        credential: PhoneAuthCredential,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure("Xác minh OTP thất bại: ${task.exception?.message}")
                }
            }
    }

    // Lưu dữ liệu người dùng vào Firestore (dùng cho đăng ký)
    fun saveUserData(
        phoneNumber: String,
        userName: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userData = hashMapOf(
            "displayName" to userName,
            "creatAt" to System.currentTimeMillis().toString()
        )
        firestore.collection("USERS").document(phoneNumber)
            .set(userData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure("Lưu dữ liệu thất bại: ${e.message}")
            }
    }
}