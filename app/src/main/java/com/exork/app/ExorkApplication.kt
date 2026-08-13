package com.exork.app

import android.app.Application
import com.exork.app.util.NotificationHelper

class ExorkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
