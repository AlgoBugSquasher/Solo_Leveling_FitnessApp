package com.exork.app.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.exork.app.MainActivity
import com.exork.app.R
import com.exork.app.data.AppDatabase
import com.exork.app.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // We need to reschedule the next alarm as well for the daily repeat
        BootReceiver.scheduleDailyReminder(context)

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val quests = db.dailyQuestDao().getAllQuests().first()

            // SMART CONDITIONAL LOGIC: Only fire if incomplete
            val allCompleted = quests.isNotEmpty() && quests.all { it.isCompleted }

            if (!allCompleted) {
                sendNotification(context)
            }
        }
    }

    private fun sendNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = NotificationHelper.CHANNEL_ID

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🚨 SYSTEM ALERT")
            .setContentText("Daily Quest incomplete. Penalty Zone awaits! Complete your training now, Hunter!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(7000, notification)
    }
}
