package com.example.pbl5.utils

object Utils {
    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.length == 10 && phone.all { it.isDigit() }
    }
}