package id.apwdevs.app.catalogue.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import java.lang.ref.WeakReference
import java.util.*

class BaseJobManager private constructor(context: Context) {

    private val weakContext = WeakReference(context)

    fun start(position: Int, atTime: Calendar) {
        weakContext.get()?.apply {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingJobs = ScheduleContract.buildIntoPendingIntent(this, position, atTime)
            pendingJobs?.let {
                if (isRunning(this, atTime, position))
                    cancel(position, atTime)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, atTime.timeInMillis, it)
            }
        }
    }

    fun isMatchInSystem(position: Int): Boolean {
        weakContext.get()?.apply {
            return isRunning(this, Calendar.getInstance(), position)
        }
        return false
    }

    private fun isRunning(context: Context, atTime: Calendar, position: Int): Boolean {
        return ScheduleContract.buildIntoPendingIntent(context, position, atTime, PendingIntent.FLAG_NO_CREATE) != null
    }

    fun startAtId(id: Int, atTime: Calendar) {
        var pos = -1
        for ((idx, value) in ScheduleContract.listPendingJobs.withIndex()) {
            if (value.jobId == id) {
                pos = idx
                break
            }
        }
        start(pos, atTime)
    }

    fun startAll(atTime: Calendar) {
        for (i in ScheduleContract.listPendingJobs.indices) {
            start(i, atTime)
        }
    }

    fun cancel(position: Int, alarmOn: Calendar) {
        weakContext.get()?.apply {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingJobs =
                ScheduleContract.buildIntoPendingIntent(this, position, alarmOn, PendingIntent.FLAG_NO_CREATE)
            pendingJobs?.let {
                alarmManager.cancel(it)
            }
        }
    }

    fun cancelAll(onTime: Calendar) {
        for (i in ScheduleContract.listPendingJobs.indices) {
            cancel(i, onTime)
        }
    }

    companion object {
        @JvmStatic
        private var sInstance: BaseJobManager? = null

        @JvmStatic
        fun getInstance(context: Context): BaseJobManager =
            sInstance ?: BaseJobManager(context).apply {
                sInstance = this
            }
    }
}