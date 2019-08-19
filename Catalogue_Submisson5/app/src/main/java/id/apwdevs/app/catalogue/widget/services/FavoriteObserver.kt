package id.apwdevs.app.catalogue.widget.services

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.provider.FavoriteProvider
import id.apwdevs.app.catalogue.widget.FavoriteWidget
import java.lang.ref.WeakReference

class FavoriteObserver : Service() {

    override fun onCreate() {
        super.onCreate()
        HandlerThread("WidgetObserver").apply {
            start()
            DataWidgetObserver.getInstance(baseContext, this)?.let {
                Log.d("DataWidgetObserver", "Starts an observer on looper $looper...............")
                baseContext.contentResolver.registerContentObserver(
                    FavoriteProvider.BASE_URI_FAVORITE.build(),
                    true,
                    it
                )
            }
        }
        val ids = AppWidgetManager.getInstance(baseContext)
        ids.notifyAppWidgetViewDataChanged(
            ids.getAppWidgetIds(ComponentName(baseContext, FavoriteWidget::class.java)),
            R.id.stackview_widget
        )
    }

    override fun onDestroy() {
        DataWidgetObserver.forceGetInstance()?.let {
            Log.d("DataWidgetObserver", "Destroy observer")
            baseContext.contentResolver.unregisterContentObserver(it)
            DataWidgetObserver.destroy()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return Binder()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (rootIntent != null)
            startService(rootIntent)
        else
            startService(Intent(this, FavoriteObserver::class.java))
    }

    class DataWidgetObserver private constructor(context: Context, internal val handlerThread: HandlerThread) :
        ContentObserver(
            Handler(handlerThread.looper)
        ) {

        private val weakContext = WeakReference(context)
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            Log.d("DataWidgetObserver", "Hurry Up! Any Change on database, notify all widgets! ${weakContext.get()}")
            weakContext.get()?.let {
                it.sendBroadcast(Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    `package` = it.packageName
                    putExtra(FavoriteWidget.UPDATE_TYPE, FavoriteWidget.STACK_UPDATE_ALL)
                })
            }

        }

        companion object {

            @JvmStatic
            private var instance: DataWidgetObserver? = null

            @JvmStatic
            fun getInstance(context: Context, handlerThread: HandlerThread): DataWidgetObserver? {
                if (instance == null) {
                    instance = DataWidgetObserver(context, handlerThread)
                }
                return instance
            }

            @JvmStatic
            fun forceGetInstance(): DataWidgetObserver? {
                return instance
            }

            @JvmStatic
            fun destroy() {
                instance?.handlerThread?.quit()
                instance = null
                System.gc()
            }
        }
    }
}
