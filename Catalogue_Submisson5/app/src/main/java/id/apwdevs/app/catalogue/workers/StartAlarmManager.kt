package id.apwdevs.app.catalogue.workers

import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.work.Worker
import androidx.work.WorkerParameters
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.manager.BaseJobManager
import id.apwdevs.app.catalogue.plugin.PublicContract
import java.text.SimpleDateFormat
import java.util.*

class StartAlarmManager(context: Context, jobParams: WorkerParameters) :
    Worker(context, jobParams) {
    override fun doWork(): Result {
        StartAlarmManagerContract.listPendingAlarmJob.forEach {
            applicationContext.getSharedPreferences(
                PublicContract.SHARED_PREF_GLOBAL_NAME,
                Context.MODE_PRIVATE
            )
                .apply {
                    if (it.useSharedPrefControls) {
                        val sharedVal = getBoolean(
                            applicationContext.getString(it.sharedPrefControlsName),
                            it.sharedPrefControlsDefault
                        )
                        if (!sharedVal) {
                            BaseJobManager.getInstance(applicationContext)
                                .cancel(it.lJobId, Calendar.getInstance().apply {
                                    time =
                                        SimpleDateFormat(it.dateFormat, Locale.getDefault()).parse(
                                            getString(it.timeSharedPrefName, it.date) ?: it.date
                                        )
                                })
                            return@apply
                        }
                    }

                    val alarmTm = if (it.useTimeSharedPreference && it.timeSharedPrefName != null) {
                        getString(it.timeSharedPrefName, it.date) ?: it.date
                    } else {
                        it.date
                    }
                    val calendarTime = Calendar.getInstance()
                    val timeNow = calendarTime.timeInMillis
                    calendarTime.time =
                        SimpleDateFormat(it.dateFormat, Locale.getDefault()).parse(alarmTm)
                    while (it.allowNextReschedule && timeNow > calendarTime.timeInMillis) {
                        calendarTime.add(Calendar.DAY_OF_MONTH, 1)
                    }
                    BaseJobManager.getInstance(applicationContext).start(it.lJobId, calendarTime)

                    saveSharedPrefIfNoKey(this, it)

                    Log.d(
                        "StartAlarmManagerWorker",
                        "Starts Alarm Job At ${it.lJobId} with time $calendarTime , timeInMillis = ${calendarTime.timeInMillis}"
                    )
                }
        }
        android.os.Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                applicationContext,
                applicationContext.getString(
                    R.string.applying_reminder,
                    StartAlarmManagerContract.listPendingAlarmJob.size
                ),
                Toast.LENGTH_SHORT
            ).show()
        }
        return Result.success()
    }

    private fun saveSharedPrefIfNoKey(
        sharedPreferences: SharedPreferences?,
        it: StartAlarmManagerContract.AlarmIdentity
    ) {
        sharedPreferences?.apply {
            edit(commit = true) {
                val enableStr = applicationContext.getString(it.sharedPrefControlsName)
                val timeStr = it.timeSharedPrefName
                if (!contains(enableStr) && it.useSharedPrefControls)
                    putBoolean(enableStr, it.sharedPrefControlsDefault)
                if (!contains(timeStr) && it.useTimeSharedPreference)
                    putString(timeStr, it.date)
            }

        }
    }

}