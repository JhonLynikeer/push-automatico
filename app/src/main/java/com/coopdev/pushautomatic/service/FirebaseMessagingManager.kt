package com.coopdev.pushautomatic.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FirebaseMessagingManager {
    private val TAG = "FirebaseMsgManager"

    suspend fun getToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "Token FCM obtido: $token")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter token FCM", e)
            null
        }
    }

    fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Inscrito no tópico: $topic")
                } else {
                    Log.e(TAG, "Falha ao se inscrever no tópico: $topic")
                }
            }
    }
} 