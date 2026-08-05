package com.example.myapplication.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DailyQuestWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE || 
            intent.action == "com.example.myapplication.ACTION_DATA_UPDATED") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, DailyQuestWidget::class.java))
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        try {
            val views = RemoteViews(context.packageName, R.layout.widget_daily_quest)

            // Launch app on click
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            // Fetch data asynchronously
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val user = db.userDao().getUser().first()
                    val quests = db.dailyQuestDao().getAllQuests().first()

                    launch(Dispatchers.Main) {
                        try {
                            user?.let {
                                views.setTextViewText(R.id.widget_streak, "🔥 ${it.streak} DAYS")
                            }

                            if (quests.isNotEmpty()) {
                                // Update Quest 1
                                quests.getOrNull(0)?.let { q ->
                                    views.setTextViewText(R.id.widget_label_1, "${q.title} [ ${q.currentProgress} / ${q.targetValue} ]")
                                    views.setProgressBar(R.id.widget_progress_1, q.targetValue, q.currentProgress, false)
                                }
                                
                                // Update Quest 2
                                quests.getOrNull(1)?.let { q ->
                                    views.setTextViewText(R.id.widget_label_2, "${q.title} [ ${q.currentProgress} / ${q.targetValue} ]")
                                    views.setProgressBar(R.id.widget_progress_2, q.targetValue, q.currentProgress, false)
                                }
                                
                                // Update Quest 3
                                quests.getOrNull(2)?.let { q ->
                                    views.setTextViewText(R.id.widget_label_3, "${q.title} [ ${q.currentProgress} / ${q.targetValue} ]")
                                    views.setProgressBar(R.id.widget_progress_3, q.targetValue, q.currentProgress, false)
                                }

                                val allCompleted = quests.all { it.isCompleted }
                                if (allCompleted) {
                                    views.setTextViewText(R.id.widget_status, "⚡ QUEST CLEARED")
                                    views.setTextColor(R.id.widget_status, android.graphics.Color.parseColor("#39FF14")) // Neon Green
                                } else {
                                    views.setTextViewText(R.id.widget_status, "🚨 INCOMPLETE")
                                    views.setTextColor(R.id.widget_status, android.graphics.Color.parseColor("#FF3131")) // Neon Red
                                }
                            }

                            appWidgetManager.updateAppWidget(appWidgetId, views)
                        } catch (e: Exception) {
                            android.util.Log.e("DailyQuestWidget", "UI mapping failed", e)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("DailyQuestWidget", "Data fetch failed", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DailyQuestWidget", "Widget update failed", e)
        }
    }
}
