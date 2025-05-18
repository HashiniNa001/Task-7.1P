package com.example.lostandfoundapp

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class LostAndFoundApplication : Application() {
    // Create a custom scope for the application
    val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        // Initialize any application-wide components here
    }
} 