package com.example.myapplication

import android.app.Application
import com.example.myapplication.util.NotificationHelper

class ExorkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
