package com.coopdev.pushautomatic.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.coopdev.pushautomatic.model.ScheduledNotification
import com.coopdev.pushautomatic.receiver.AlarmReceiver
import java.util.Calendar
import android.util.Log

class NotificationScheduler(private val context: Context) {
    private val TAG = "NotificationScheduler"
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleNotifications(notifications: List<ScheduledNotification>) {
        Log.d(TAG, "Iniciando agendamento de ${notifications.size} notificações")
        cancelAllNotifications()

        notifications.forEach { notification ->
            if (notification.enabled) {
                Log.d(TAG, "Agendando notificação: ID=${notification.id}, Hora=${notification.hour}:${notification.minute}")
                scheduleNotification(notification)
            } else {
                Log.d(TAG, "Notificação desabilitada: ID=${notification.id}")
            }
        }
    }

    private fun scheduleNotification(notification: ScheduledNotification) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("notification_id", notification.id)
            putExtra("notification_title", notification.title)
            putExtra("notification_message", notification.message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notification.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Configura o horário da notificação
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, notification.hour)
            set(Calendar.MINUTE, notification.minute)
            set(Calendar.SECOND, 0)
            
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
                Log.d(TAG, "Horário já passou hoje, agendando para amanhã: ID=${notification.id}")
            }
        }

        Log.d(TAG, "Agendando alarme para: ${calendar.time}, ID=${notification.id}")
        
        // Agenda a notificação para repetir diariamente
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelAllNotifications() {
        Log.d(TAG, "Cancelando todas as notificações existentes")
        // Cancela todas as notificações existentes
        for (id in 1..100) { // Assume um máximo de 100 notificações
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
} 