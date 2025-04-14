package com.coopdev.pushautomatic.service

import android.util.Log
import com.coopdev.pushautomatic.notification.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "FirebaseMsgService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Mensagem recebida do FCM")

        // Verifica se a mensagem contém dados
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Dados da mensagem: ${remoteMessage.data}")
        }

        // Verifica se a mensagem contém notificação
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "Nova mensagem"
            val body = notification.body ?: "Você tem uma nova mensagem"
            val deepLink = remoteMessage.data["deepLink"]

            Log.d(TAG, "Título: $title")
            Log.d(TAG, "Corpo: $body")
            Log.d(TAG, "DeepLink: $deepLink")

            // Mostra a notificação
            NotificationHelper(this).showNotification(title, body, deepLink)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Novo token FCM: $token")
        // Aqui você pode enviar o token para seu servidor
    }
} 