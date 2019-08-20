package id.apwdevs.app.catalogue.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.activities.DetailActivity
import id.apwdevs.app.catalogue.activities.MainTabUserActivity
import id.apwdevs.app.catalogue.activities.StackWidgetPreferenceActivity
import id.apwdevs.app.catalogue.database.FavoriteDatabase
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.widget.services.FavoriteObserver
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
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.getStringExtra(ACTION_TYPE)) {
            STACK_UPDATE_ALL -> context?.let {
                val wInstance = AppWidgetManager.getInstance(it)
                val update = wInstance.getAppWidgetIds(ComponentName(it, FavoriteWidget::class.java))
                wInstance.notifyAppWidgetViewDataChanged(update, R.id.stackview_widget)
            }
            STACK_LAUNCH_DETAIL_ITEM -> context?.let {
                GlobalScope.launch {
                    val cId = intent.getIntExtra(STACK_ITEM_ID, -1)
                    if (cId == -1) return@launch
                    val favDao = FavoriteDatabase.getInstance(context).favoriteDao()
                    favDao.getItemAt(cId)?.let {
                        context.startActivity(
                            Intent(context, DetailActivity::class.java).apply {
                                putExtras(Bundle().also { mExtra ->
                                    mExtra.putParcelable(
                                        DetailActivity.EXTRA_DETAIL_TYPES,
                                        PublicContract.ContentDisplayType.FAVORITES
                                    )
                                    mExtra.putInt(DetailActivity.EXTRA_ID, it.id)
                                    mExtra.putParcelable(DetailActivity.EXTRA_CONTENT_DETAILS, it)
                                })
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        )


                    }
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
    companion object {

        const val START_OBSERVER_AND_UPDATE: String = "OBSERVER_START"
        const val STACK_ITEM_ID: String = "STACK_ID"
        const val STACK_LAUNCH_DETAIL_ITEM: String = "REMOVE_FAVORITE_ITEM"

        const val STACK_UPDATE_ALL: String = "REQ_UPDATE_ALL"
        const val ACTION_TYPE: String = "APPWIDGET_UPDATE_TYPE"
        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            appWidgetManager.updateAppWidget(
                appWidgetId,
                RemoteViews(context.packageName, R.layout.favorite_widget).apply {
                    setRemoteAdapter(R.id.stackview_widget, Intent(context, StackWidgetService::class.java).also {
                        it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        it.data = Uri.parse(it.toUri(Intent.URI_INTENT_SCHEME))
                    })
                    setEmptyView(R.id.stackview_widget, R.id.empty_views)
                    val idType =
                        context.getSharedPreferences(PublicContract.WIDGET_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                            .getInt("widget_conf_${appWidgetId}_type", -1)
                    setTextViewText(
                        R.id.text_display, when (PublicContract.ContentDisplayType.findId(idType)) {
                            PublicContract.ContentDisplayType.TV_SHOWS -> "My Favorite Tv Shows"
                            PublicContract.ContentDisplayType.MOVIE -> "My Favorite Movies"
                            else -> "Whatever :("
                        }
                    )
                    setOnClickPendingIntent(
                        R.id.btn_goto_app,
                        PendingIntent.getActivity(
                            context,
                            0xca2f,
                            Intent(context, MainTabUserActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            },
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                    setOnClickPendingIntent(
                        R.id.widget_display,
                        PendingIntent.getActivity(
                            context,
                            0xaafa,
                            Intent(context, StackWidgetPreferenceActivity::class.java).apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                putExtra(StackWidgetPreferenceActivity.OWN_UPDATE, true)
                            },
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )

                })
        }
    }
}

