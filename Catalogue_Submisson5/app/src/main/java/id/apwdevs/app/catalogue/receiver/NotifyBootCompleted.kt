package id.apwdevs.app.catalogue.receiver

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import id.apwdevs.app.catalogue.widget.FavoriteWidget
import id.apwdevs.app.catalogue.workers.StartAlarmManager

class NotifyBootCompleted : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        // Starts all-defined alarmManager
        context?.let {
            WorkManager.getInstance(it).enqueue(
                OneTimeWorkRequest.Builder(StartAlarmManager::class.java).build()
            )

            // update the widget
            startObserverIfAny(it)
        }

        Log.d("NotifyBootCompleted", "Success applying all defined jobs")
    }

    private fun startObserverIfAny(it: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(it)
        val lContents = appWidgetManager.getAppWidgetIds(ComponentName(it, FavoriteWidget::class.java))
        if (lContents.isEmpty()) return
        it.sendBroadcast(
            Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                `package` = it.packageName
                putExtra(FavoriteWidget.UPDATE_TYPE, FavoriteWidget.START_OBSERVER_AND_UPDATE)
            }
        )
    }
}
