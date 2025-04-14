package com.coopdev.pushautomatic

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.coopdev.pushautomatic.notification.NotificationHelper
import com.coopdev.pushautomatic.receiver.AlarmReceiver
import com.coopdev.pushautomatic.config.RemoteConfigManager
import com.coopdev.pushautomatic.notification.NotificationScheduler
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.lifecycle.lifecycleScope
import com.coopdev.pushautomatic.ui.DeepLink1Fragment
import com.coopdev.pushautomatic.ui.DeepLink2Fragment
import com.coopdev.pushautomatic.service.FirebaseMessagingManager

class MainActivity : AppCompatActivity() {

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var alarmManager: AlarmManager
    private val NOTIFICATION_PERMISSION_CODE = 123
    private lateinit var remoteConfig: RemoteConfigManager
    private lateinit var notificationScheduler: NotificationScheduler
    private lateinit var firebaseMessagingManager: FirebaseMessagingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        notificationHelper = NotificationHelper(this)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        remoteConfig = RemoteConfigManager()
        notificationScheduler = NotificationScheduler(this)
        firebaseMessagingManager = FirebaseMessagingManager()

        // Inicializa o Firebase Messaging
        lifecycleScope.launch {
            val token = firebaseMessagingManager.getToken()
            if (token != null) {
                // Inscreve em um tópico para receber notificações
                firebaseMessagingManager.subscribeToTopic("notificacoes")
            }
        }

        // Atualiza as configurações e agenda as notificações
        lifecycleScope.launch {
            if (remoteConfig.fetchAndActivate()) {
                val notifications = remoteConfig.getScheduledNotifications()
                notificationScheduler.scheduleNotifications(notifications)

                Toast.makeText(
                    this@MainActivity,
                    "Notificações agendadas com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        findViewById<Button>(R.id.btnTestNotification).setOnClickListener {
            if (checkNotificationPermission()) {
                // Agenda todas as notificações configuradas no Remote Config
                lifecycleScope.launch {
                    val notifications = remoteConfig.getScheduledNotifications()
                    notificationScheduler.scheduleNotifications(notifications)
                    Toast.makeText(
                        this@MainActivity,
                        "Notificações agendadas!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        findViewById<Button>(R.id.btnCancelNotifications).setOnClickListener {
            notificationScheduler.cancelAllNotifications()
            Toast.makeText(
                this,
                "Notificações canceladas",
                Toast.LENGTH_SHORT
            ).show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Verificar se foi aberto por uma ação específica
        when (intent?.action) {
            "VIEW_ACTION" -> {
                Toast.makeText(this, "Aberto pela ação Visualizar", Toast.LENGTH_SHORT).show()
                // Faça algo específico para esta ação
            }
        }

        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        when (intent?.data?.host) {
            "deeplink1" -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, DeepLink1Fragment())
                    .addToBackStack(null)
                    .commit()
            }
            "deeplink2" -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, DeepLink2Fragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun checkNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Agenda as notificações quando a permissão é concedida
                lifecycleScope.launch {
                    val notifications = remoteConfig.getScheduledNotifications()
                    notificationScheduler.scheduleNotifications(notifications)
                }
            } else {
                Toast.makeText(
                    this,
                    "Permissão de notificação negada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
} 