package id.apwdevs.app.catalogue.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import id.apwdevs.app.catalogue.manager.BaseJobManager
import id.apwdevs.app.catalogue.manager.ScheduleContract
import java.util.*
import java.util.concurrent.TimeUnit

class StartExactJobReceiver : BroadcastReceiver() {

    companion object {
        const val MAX_RETRY_KEY = "MAX_RETRY"
        const val JOB_ID = "JOB_ID"
        const val SHOULD_RETRY_KEY = "RETRY_KEY"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val pendingJob =
            ScheduleContract.getObject(intent.getBundleExtra(ScheduleContract.JOB_SELF_PARAMS))
        val posCls = pendingJob.workJobClassPos
        if (posCls < 0) return

        @Suppress("UNCHECKED_CAST")
        val clsWorker = ScheduleContract.listRunnerCls[posCls] as Class<Worker>
        // starts a specific jobs
        val task = OneTimeWorkRequest.Builder(clsWorker).apply {
            setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 20, TimeUnit.SECONDS)
            val data = Data.Builder().apply {
                putInt(JOB_ID, pendingJob.jobId)
                putInt(MAX_RETRY_KEY, pendingJob.maxRetryCount)
                putBoolean(SHOULD_RETRY_KEY, pendingJob.retryWhenJobFail)
            }
            setInputData(data.build())
        }
        WorkManager.getInstance(context).enqueue(task.build())

        // should schedule again? if yes, schedule again
        if (pendingJob.hasRecurringAfterCalled) {
            val newCalendar = Calendar.getInstance()
            newCalendar.time =
                Date(
                    intent.getLongExtra(
                        ScheduleContract.RUN_EXACT_TIME_NOW,
                        System.currentTimeMillis()
                    )
                )
            newCalendar.add(Calendar.DAY_OF_MONTH, 1)
            BaseJobManager.getInstance(context).startAtId(pendingJob.jobId, newCalendar)
        }
    }
}
