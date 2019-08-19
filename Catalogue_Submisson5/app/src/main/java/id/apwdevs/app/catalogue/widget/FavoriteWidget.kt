package id.apwdevs.app.catalogue.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.activities.MainTabUserActivity
import id.apwdevs.app.catalogue.provider.FavoriteProvider.Companion.BASE_URI_FAVORITE
import id.apwdevs.app.catalogue.widget.services.FavoriteObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
        context.startService(Intent(context, FavoriteObserver::class.java))

    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        context.stopService(Intent(context, FavoriteObserver::class.java))
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.getStringExtra(UPDATE_TYPE)) {
            STACK_UPDATE_ALL -> context?.let {
                val wInstance = AppWidgetManager.getInstance(it)
                val update = wInstance.getAppWidgetIds(ComponentName(it, FavoriteWidget::class.java))
                wInstance.notifyAppWidgetViewDataChanged(update, R.id.stackview_widget)
            }
            STACK_REMOVE_ITEM -> context?.let {
                GlobalScope.launch(Dispatchers.IO) {
                    if (removeFromFavorite(it, intent.getIntExtra(STACK_ITEM_ID, -1)) >= -1)
                        it.contentResolver.notifyChange(
                            BASE_URI_FAVORITE.build(),
                            FavoriteObserver.DataWidgetObserver.forceGetInstance()
                        )
                }

            }
            START_OBSERVER_AND_UPDATE -> context?.let {
                val wInstance = AppWidgetManager.getInstance(it)
                val allIds = wInstance.getAppWidgetIds(ComponentName(it, FavoriteWidget::class.java))
                onEnabled(it)
                onUpdate(it, wInstance, allIds)
            }
            else -> super.onReceive(context, intent)
        }

    }

    fun removeFromFavorite(context: Context, id: Int): Int =
        context.contentResolver.delete(BASE_URI_FAVORITE.appendPath(id.toString()).build(), null, null)

    companion object {

        const val START_OBSERVER_AND_UPDATE: String = "OBSERVER_START"
        const val STACK_ITEM_ID: String = "STACK_ID"
        const val STACK_REMOVE_ITEM: String = "REMOVE_FAVORITE_ITEM"
        private val TOAST_ACTION: String = "HHHH"

        const val STACK_UPDATE_ALL: String = "REQ_UPDATE_ALL"
        const val UPDATE_TYPE: String = "APPWIDGET_UPDATE_TYPE"
        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {

            // Construct the RemoteViews object
            val intent = Intent(context, StackWidgetService::class.java).also {
                it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                it.data = Uri.parse(it.toUri(Intent.URI_INTENT_SCHEME))
            }
            val remoteViews = RemoteViews(context.packageName, R.layout.favorite_widget)
            remoteViews.setRemoteAdapter(R.id.stackview_widget, intent)
            remoteViews.setEmptyView(R.id.stackview_widget, R.id.empty_views)
            //remoteViews.setImageViewResource(R.id.widget_settings, R.drawable.ic_settings_black_24dp)
            //remoteViews.setTextColor(R.id.title_display, Color.WHITE)
            remoteViews.setTextViewText(R.id.text_display, "My Favorite Movie")
            remoteViews.setOnClickPendingIntent(
                R.id.btn_goto_app,
                PendingIntent.getActivity(context, 0xca2f, Intent(context, MainTabUserActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }, PendingIntent.FLAG_UPDATE_CURRENT)
            )

            val toastIntent = Intent(context, FavoriteWidget::class.java).also {
                it.action = FavoriteWidget.TOAST_ACTION
                it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }

            remoteViews.setPendingIntentTemplate(
                R.id.stackview_widget,
                PendingIntent.getBroadcast(context, 0, toastIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            )


            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }
}

