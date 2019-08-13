package id.apwdevs.app.catalogue.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import id.apwdevs.app.catalogue.workers.StartAlarmManager

class NotifyBootCompleted : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        // Starts all-defined alarmManager
        context?.let {
            WorkManager.getInstance(it).enqueue(
                OneTimeWorkRequest.Builder(StartAlarmManager::class.java).build()
            )
        }
        Log.d("NotifyBootCompleted", "Success applying all defined jobs")
    }
}
