package com.habit.app.ui.setting

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.habit.app.MyApplication
import com.habit.app.R
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.MainActivity

class SearchWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        if (context == null || appWidgetManager == null || appWidgetIds == null) {
            return
        }
        for (appWidgetId in appWidgetIds) {
            updateWidgetUi(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        const val WIDGET_EXTRA = "widget_extra"
        const val EXTRA_HOME = "home"
        const val EXTRA_SEARCH = "search"
        const val EXTRA_NEWS = "news"
        const val EXTRA_SCAN = "scan"

        fun updateWidgetTheme() {
            val appWidgetManager = AppWidgetManager.getInstance(MyApplication.mContext)
            val componentName = ComponentName(MyApplication.mContext, SearchWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            for (appWidgetId in ids) {
                updateWidgetUi(MyApplication.mContext, appWidgetManager, appWidgetId)
            }
        }

        fun updateWidgetUi(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.layout_search_widget)
            views.setInt(R.id.widget_root, "setBackgroundResource", if (ThemeManager.isNightTheme()) R.drawable.t_night_widget_bg else R.drawable.widget_bg)
            views.setImageViewResource(R.id.iv_search, ThemeManager.getSkinImageResId(R.drawable.iv_widget_search))
            views.setImageViewResource(R.id.iv_news, ThemeManager.getSkinImageResId(R.drawable.iv_widget_news))
            views.setImageViewResource(R.id.iv_scan, ThemeManager.getSkinImageResId(R.drawable.iv_widget_scan))

            val homeIntent = Intent(context, MainActivity::class.java).apply {
                putExtra(WIDGET_EXTRA, EXTRA_HOME)
            }
            val homePendingIntent = PendingIntent.getActivity(context, 0, homeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_root, homePendingIntent)

            // 设置点击事件
            val searchIntent = Intent(context, MainActivity::class.java).apply {
                putExtra(WIDGET_EXTRA, EXTRA_SEARCH)
            }
            val searchPendingIntent = PendingIntent.getActivity(context, 1, searchIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.iv_search, searchPendingIntent)

            val newsIntent = Intent(context, MainActivity::class.java).apply {
                putExtra(WIDGET_EXTRA, EXTRA_NEWS)
            }
            val newsPendingIntent = PendingIntent.getActivity(context, 2, newsIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.iv_news, newsPendingIntent)

            val scanIntent = Intent(context, MainActivity::class.java).apply {
                putExtra(WIDGET_EXTRA, EXTRA_SCAN)
            }
            val scanPendingIntent = PendingIntent.getActivity(context, 3, scanIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.iv_scan, scanPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}