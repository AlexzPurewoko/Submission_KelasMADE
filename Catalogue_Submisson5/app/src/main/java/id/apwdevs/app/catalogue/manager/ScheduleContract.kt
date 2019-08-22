package id.apwdevs.app.catalogue.manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import id.apwdevs.app.catalogue.receiver.StartExactJobReceiver
import id.apwdevs.app.catalogue.workers.DailyReminderNotif
import id.apwdevs.app.catalogue.workers.ReleaseTodayReminder
import java.util.*

object ScheduleContract {
    const val RUN_EXACT_TIME_NOW: String = "JOB_TIME_MILLIS"
    private const val JOB_ID = "JOB_ID"
    private const val JOB_TAG = "JOB_TAG"
    private const val JOB_CLS_LIST_POS = "JOT_CLS_POS"
    private const val JOB_RECURRING = "JOB_RECURING"
    private const val JOB_RETRY_WHEN_FAIL = "JOB_RETRY_WHEN_FAIL"
    private const val JOB_MAX_RETRY_COUNT = "JOB_MAX_RETRY"
    const val JOB_SELF_PARAMS: String = "JOB_PARAMETERS"
    val listPendingJobs: List<PendingAlarmRunJob> = listOf(
        // put your all service here
        PendingAlarmRunJob(
            0x44a,
            "DailyReminder",
            0,
            hasRecurringAfterCalled = true,
            retryWhenJobFail = false
        ),
        PendingAlarmRunJob(
            0x4ab,
            "DailyReleaseToday",
            1,
            hasRecurringAfterCalled = true,
            retryWhenJobFail = false
        )
    )
    val listRunnerCls: List<Class<*>> = listOf(
        DailyReminderNotif::class.java,
        ReleaseTodayReminder::class.java
    )

    fun buildIntoPendingIntent(context: Context, position: Int, calendar: Calendar, flags: Int = 0): PendingIntent? {
        if (position !in 0 until listPendingJobs.size) return null
        val pendingJob = listPendingJobs[position]
        val intent = Intent(context, StartExactJobReceiver::class.java).apply {
            putExtra(JOB_SELF_PARAMS, putIntoBundle(pendingJob))
            putExtra(RUN_EXACT_TIME_NOW, calendar.timeInMillis)
        }
        return PendingIntent.getBroadcast(context, pendingJob.jobId, intent, flags)
    }

    private fun putIntoBundle(pendingJob: PendingAlarmRunJob): Bundle = Bundle().apply {
        pendingJob.apply {
            putInt(JOB_ID, jobId)
            putString(JOB_TAG, jobTags)
            putInt(JOB_CLS_LIST_POS, workJobClassPos)
            putBoolean(JOB_RECURRING, hasRecurringAfterCalled)
            putBoolean(JOB_RETRY_WHEN_FAIL, retryWhenJobFail)
            putInt(JOB_MAX_RETRY_COUNT, maxRetryCount)
        }
    }

    fun getObject(bundle: Bundle): PendingAlarmRunJob {
        bundle.apply {
            return PendingAlarmRunJob(
                getInt(JOB_ID, 0),
                getString(JOB_TAG, ""),
                getInt(JOB_CLS_LIST_POS, -1),
                getBoolean(JOB_RECURRING, false),
                getBoolean(JOB_RETRY_WHEN_FAIL, false),
                getInt(JOB_MAX_RETRY_COUNT, 0)
            )

        }
    }
}
