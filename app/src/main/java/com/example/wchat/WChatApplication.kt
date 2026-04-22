package com.example.wchat

import android.app.Application
import com.example.wchat.services.AppLifecycleTracker
import com.google.firebase.FirebaseApp

class WChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppLifecycleTracker.initialize(this)
    }
}