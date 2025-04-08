package com.coopdev.pushautomatic.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.coopdev.pushautomatic.notification.NotificationHelper

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Notificação Automática"
        val message = inputData.getString("message") ?: "Esta é uma notificação automática!"

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(title, message)

        return Result.success()
    }
} 