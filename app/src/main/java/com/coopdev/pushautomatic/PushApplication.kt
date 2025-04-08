package com.coopdev.pushautomatic

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class PushApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Inicializa o Firebase
        FirebaseApp.initializeApp(this)
    }
} 