package com.coopdev.pushautomatic.config

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await
import com.coopdev.pushautomatic.model.ScheduledNotification
import com.google.firebase.FirebaseApp
import com.coopdev.pushautomatic.PushApplication

class RemoteConfigManager {
    private val TAG = "RemoteConfigManager"
    private val remoteConfig by lazy {
        try {
            // Verifica se o Firebase está inicializado
            if (FirebaseApp.getApps(PushApplication.instance).isEmpty()) {
                FirebaseApp.initializeApp(PushApplication.instance)
                Log.d(TAG, "Firebase inicializado no RemoteConfigManager")
            }
            Firebase.remoteConfig
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar Firebase no RemoteConfigManager", e)
            throw e
        }
    }
    private val gson = Gson()

    init {
        try {
            Log.d(TAG, "Iniciando configuração do Remote Config")
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0 // Para teste, depois mude para 3600
            }
            remoteConfig.setConfigSettingsAsync(configSettings)

            // JSON padrão para as notificações
            val defaultNotifications = """
            {
              "scheduled_notifications": [
                {
                  "id": 1,
                  "hour": 10,
                  "minute": 30,
                  "title": "Compre Agora!",
                  "message": "Estamos em promoção!",
                  "enabled": true
                }
              ]
            }
            """.trimIndent()

            Log.d(TAG, "Configurando valores padrão: $defaultNotifications")
            
            val defaults = mapOf(
                "notifications_config" to defaultNotifications
            )
            remoteConfig.setDefaultsAsync(defaults)

            Log.d(TAG, "Remote Config configurado com sucesso")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao configurar Remote Config", e)
        }
    }

    suspend fun fetchAndActivate(): Boolean {
        return try {
            Log.d(TAG, "Iniciando fetch do Remote Config")
            val result = remoteConfig.fetchAndActivate().await()
            Log.d(TAG, "Fetch concluído com sucesso: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao fazer fetch do Remote Config", e)
            false
        }
    }

    fun getScheduledNotifications(): List<ScheduledNotification> {
        return try {
            val json = remoteConfig.getString("notifications_config")
            Log.d(TAG, "JSON recebido do Remote Config: $json")
            
            val type = object : TypeToken<NotificationsConfigWrapper>() {}.type
            val wrapper = gson.fromJson<NotificationsConfigWrapper>(json, type)
            
            Log.d(TAG, "Notificações parseadas: ${wrapper.notifications_config.scheduled_notifications.size}")
            wrapper.notifications_config.scheduled_notifications.forEach { notification ->
                Log.d(TAG, "Notificação: ID=${notification.id}, Hora=${notification.hour}:${notification.minute}, Título=${notification.title}")
            }
            
            wrapper.notifications_config.scheduled_notifications
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter notificações do Remote Config", e)
            emptyList()
        }
    }

    private data class NotificationsConfig(
        val scheduled_notifications: List<ScheduledNotification>
    )

    private data class NotificationsConfigWrapper(
        val notifications_config: NotificationsConfig
    )
} 