package com.exork.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.exork.app.worker.NotificationWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleDailyReminder(context)
        }
    }

    companion object {
        fun scheduleDailyReminder(context: Context) {
            // Standardize on WorkManager for reliable background execution and easy cancellation
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 19) // 7:00 PM
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            
            var delay = calendar.timeInMillis - System.currentTimeMillis()
            if (delay < 0) delay += TimeUnit.DAYS.toMillis(1)

            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag("DAILY_QUEST_REMINDER_TAG")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "DAILY_QUEST_REMINDER",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
