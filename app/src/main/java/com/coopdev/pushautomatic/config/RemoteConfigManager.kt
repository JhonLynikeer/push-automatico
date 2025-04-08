package com.coopdev.pushautomatic.config

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await
import com.coopdev.pushautomatic.model.ScheduledNotification
import com.google.firebase.ktx.BuildConfig

class RemoteConfigManager {
    private val remoteConfig = Firebase.remoteConfig
    private val gson = Gson()

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
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

        val defaults = mapOf(
            "notifications_config" to defaultNotifications
        )
        remoteConfig.setDefaultsAsync(defaults)
    }

    suspend fun fetchAndActivate(): Boolean {
        return try {
            remoteConfig.fetchAndActivate().await()
        } catch (e: Exception) {
            false
        }
    }

    fun getScheduledNotifications(): List<ScheduledNotification> {
        return try {
            val json = remoteConfig.getString("notifications_config")
            val type = object : TypeToken<NotificationsConfig>() {}.type
            val config = gson.fromJson<NotificationsConfig>(json, type)
            config.scheduled_notifications
        } catch (e: Exception) {
            emptyList()
        }
    }

    private data class NotificationsConfig(
        val scheduled_notifications: List<ScheduledNotification>
    )
} 