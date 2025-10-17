package com.example.myapplication

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitsWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, HabitsWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            onUpdate(context, manager, ids)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.myapplication.ACTION_REFRESH_WIDGET"

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_habits)
            val prefs = context.getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)
            val json = prefs.getString("habits_json", "[]")
            val arr = JSONArray(json)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            var total = 0
            var done = 0
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                total++
                val dates = o.optJSONArray("completedDates") ?: JSONArray()
                for (j in 0 until dates.length()) {
                    if (dates.getString(j) == today) { done++; break }
                }
            }
            val percent = if (total == 0) 0 else (done * 100 / total)
            views.setTextViewText(R.id.widgetText, "Today's habits: $done/$total ($percent%)")

            val intent = Intent(context, MainActivity::class.java)
            val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widgetRoot, pi)

            val refreshIntent = Intent(context, HabitsWidgetProvider::class.java).apply { action = ACTION_REFRESH }
            val refreshPI = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widgetRefresh, refreshPI)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}


