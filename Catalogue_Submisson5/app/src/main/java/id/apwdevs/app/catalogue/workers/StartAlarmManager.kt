package id.apwdevs.app.catalogue.workers

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import id.apwdevs.app.catalogue.manager.BaseJobManager
import id.apwdevs.app.catalogue.plugin.PublicContract
import java.text.SimpleDateFormat
import java.util.*

class StartAlarmManager(context: Context, jobParams: WorkerParameters) : Worker(context, jobParams) {
    override fun doWork(): Result {
        StartAlarmManagerContract.listPendingAlarmJob.forEach {
            applicationContext.getSharedPreferences(PublicContract.SHARED_PREF_GLOBAL_NAME, Context.MODE_PRIVATE)
                .apply {
                    if (it.useSharedPrefControls) {
                        val sharedVal = getBoolean(
                            applicationContext.getString(it.sharedPrefControlsName),
                            it.sharedPrefControlsDefault
                        )
                        if (!sharedVal)
                            return@apply
                    }

                    val alarmTm: String
                    if (it.useTimeSharedPreference && it.timeSharedPrefName != null) {
                        alarmTm = getString(it.timeSharedPrefName, it.date) ?: it.date
                    } else {
                        alarmTm = it.date
                    }
                    val calendarTime = Calendar.getInstance()
                    val timeNow = calendarTime.timeInMillis
                    calendarTime.time = SimpleDateFormat(it.dateFormat, Locale.getDefault()).parse(alarmTm)
                    while (it.allowNextReschedule && timeNow > calendarTime.timeInMillis) {
                        calendarTime.add(Calendar.DAY_OF_MONTH, 1)
                    }
                    BaseJobManager.getInstance(applicationContext).start(it.lJobId, calendarTime)
                    Log.d(
                        "StartAlarmManagerWorker",
                        "Starts Alarm Job At ${it.lJobId} with time $calendarTime , timeInMillis = ${calendarTime.timeInMillis}"
                    )
                }
        }
        Toast.makeText(
            applicationContext,
            "Applied Alarm Configuration to ${StartAlarmManagerContract.listPendingAlarmJob.size} Pending Alarms successfully",
            Toast.LENGTH_SHORT
        ).show()
        return Result.success()
    }

}