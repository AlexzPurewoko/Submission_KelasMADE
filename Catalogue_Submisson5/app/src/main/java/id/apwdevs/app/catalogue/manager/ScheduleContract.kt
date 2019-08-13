package id.apwdevs.app.catalogue.manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import id.apwdevs.app.catalogue.receiver.StartExactJobReceiver
import id.apwdevs.app.catalogue.workers.DailyReminderNotif
import id.apwdevs.app.catalogue.workers.ReleaseTodayReminder
import kotlinx.android.parcel.Parcelize
import java.util.*

object ScheduleContract {
    const val RUN_EXACT_TIME_NOW: String = "JOB_TIME_MILLIS"
    const val JOB_SELF_PARAMS: String = "JOB_PARAMETERS"
    val listPendingJobs: List<PendingJobs> = listOf(
        // put your all service here
        PendingJobs(0x44a, "DailyReminder", DailyReminderNotif::class.java, true, false),
        PendingJobs(0x4ab, "DailyReleaseToday", ReleaseTodayReminder::class.java, true, false)
    )

    fun buildIntoPendingIntent(context: Context, position: Int, calendar: Calendar, flags: Int = 0): PendingIntent? {
        if (position !in 0 until listPendingJobs.size) return null
        val pendingJob = listPendingJobs[position]
        val intent = Intent(context, StartExactJobReceiver::class.java).apply {
            putExtra(JOB_SELF_PARAMS, pendingJob)
            putExtra(RUN_EXACT_TIME_NOW, calendar.timeInMillis)
        }
        return PendingIntent.getBroadcast(context, pendingJob.jobId, intent, flags)
    }

    fun getId(position: Int): Int =
        if (position !in 0 until listPendingJobs.size) -1
        else listPendingJobs[position].jobId

    fun getTags(position: Int): String? =
        if (position !in 0 until listPendingJobs.size) null
        else listPendingJobs[position].jobTags
}

@Parcelize
data class PendingJobs(
    val jobId: Int,
    val jobTags: String?,
    val workJobClass: Class<*>,
    val hasRecurringAfterCalled: Boolean,
    val retryWhenJobFail: Boolean = false,
    val maxRetryCount: Int = 2
) : Parcelable