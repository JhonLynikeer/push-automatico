package com.coopdev.pushautomatic.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.coopdev.pushautomatic.notification.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notification_id", 1)
        val title = intent.getStringExtra("notification_title") ?: "Notificação"
        val message = intent.getStringExtra("notification_message") ?: "Mensagem"

        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(title, message)
    }
} 