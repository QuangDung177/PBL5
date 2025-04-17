package com.example.pbl5.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pbl5.data.RaspberryPiRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ThresholdCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val firestore = FirebaseFirestore.getInstance()
    private val repository = RaspberryPiRepository(context, firestore)

    override suspend fun doWork(): Result {
        return try {
            // Lấy tất cả Raspberry Pis
            val raspberryPisSnapshot = firestore.collection("RASPBERRY_PIS").get().await()
            for (doc in raspberryPisSnapshot.documents) {
                val serialId = doc.id
                repository.checkAndNotify(serialId)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}