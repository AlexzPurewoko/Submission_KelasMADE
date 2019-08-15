package id.apwdevs.app.catalogue.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.widget.RemoteViews
import id.apwdevs.app.catalogue.R

/**
 * Implementation of App Widget functionality.
 */
class FavoriteWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {

        private val TOAST_ACTION: String = "HHHH"

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.favorite_widget)
            val intent = Intent(context, StackWidgetService::class.java).also {
                it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                it.data = Uri.parse(it.toUri(Intent.URI_INTENT_SCHEME))
            }
            val remoteViews = RemoteViews(context.packageName, R.layout.favorite_widget)
            //remoteViews.setRemoteAdapter(R.id.stackview_widget, intent)
            //remoteViews.setEmptyView(R.id.stackview_widget, R.id.empty_views)
            remoteViews.setImageViewResource(R.id.widget_settings, R.drawable.ic_settings_black_24dp)
            remoteViews.setTextColor(R.id.title_display, Color.WHITE)
            remoteViews.setTextViewText(R.id.title_display, "My Favorite Movie")
            //remoteViews.setOnClickPendingIntent(R.id.btn_goto_app, PendingIntent.getActivity(context, 0xca2f, Intent(context, MainTabUserActivity::class.java).apply {
            // flags = Intent.FLAG_ACTIVITY_NEW_TASK
            //}, PendingIntent.FLAG_UPDATE_CURRENT))

            val toastIntent = Intent(context, FavoriteWidget::class.java).also {
                it.action = FavoriteWidget.TOAST_ACTION
                it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }

            //remoteViews.setPendingIntentTemplate(R.id.stackview_widget, PendingIntent.getBroadcast(context, 0, toastIntent, PendingIntent.FLAG_UPDATE_CURRENT))


            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

