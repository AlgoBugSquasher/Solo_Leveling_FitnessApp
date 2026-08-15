package com.exork.app.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.exork.app.MainActivity
import com.exork.app.R
import com.exork.app.data.AppDatabase
import com.exork.app.util.NotificationHelper
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val user = db.userDao().getUser().first() ?: return Result.success()
        val quests = db.dailyQuestDao().getAllQuests().first()

        val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        
        // HARD-CHECK: Cancel notification if quest is completed today (Fail-Safe)
        if (user.lastQuestCompletedDate == todayDateString || (quests.isNotEmpty() && quests.all { it.isCompleted })) {
            return Result.success()
        }

        sendNotification()
        return Result.success()
    }

    private fun sendNotification() {
        val context = applicationContext
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

        notificationManager.notify(7001, notification)
    }
}
