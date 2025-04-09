package com.coopdev.pushautomatic

import android.app.Application
import com.google.firebase.FirebaseApp
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize

class PushApplication : Application() {
    private val TAG = "PushApplication"

    override fun onCreate() {
        super.onCreate()
        
        try {
            // Inicializa o Firebase
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d(TAG, "Firebase inicializado com sucesso")
            } else {
                Log.d(TAG, "Firebase já estava inicializado")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar Firebase", e)
        }
    }

    companion object {
        lateinit var instance: PushApplication
            private set
    }

    init {
        instance = this
    }
} 