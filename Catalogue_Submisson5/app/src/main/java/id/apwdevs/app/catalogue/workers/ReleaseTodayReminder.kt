package id.apwdevs.app.catalogue.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.media.RingtoneManager
import android.os.Build
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.core.text.set
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.activities.MainTabUserActivity
import id.apwdevs.app.catalogue.model.onUserMain.MainDataItemResponse
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.plugin.api.GetMovies
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.plugin.api.GetTVShows
import id.apwdevs.app.catalogue.receiver.StartExactJobReceiver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.util.*

class ReleaseTodayReminder(context: Context, jobParams: WorkerParameters) : CoroutineWorker(context, jobParams) {


    companion object {
        @JvmStatic
        private val GROUP_KEY_NOTIF: String = "NOTIFICATION_CATALOGUE"
        @JvmStatic
        private val NOTIF_CHANNEL_ID: String = "CurrentNotification"
        const val DISPLAY_CONTENT: String = "ALL_CONTENTS"
        const val FROM_REMINDER: Int = 0XAAFF
        const val DISPLAY_TYPE: String = "DISPLAY_TYPE"
        const val INTENT_FROM: String = "INTENT_FROM"
        const val NOTIF_ID: String = "NOTIF_ID"
    }

    @ExperimentalCoroutinesApi
    override suspend fun doWork(): Result {
        val shouldRetryKey: Boolean
        val jobId: Int
        var maxRetryKey: Int
        val sharedPref =
            applicationContext.getSharedPreferences(PublicContract.SHARED_PREF_GLOBAL_NAME, Context.MODE_PRIVATE)
        inputData.apply {
            jobId = getInt(StartExactJobReceiver.JOB_ID, 0)
            maxRetryKey = getInt(StartExactJobReceiver.MAX_RETRY_KEY, 2)
            shouldRetryKey = getBoolean(StartExactJobReceiver.SHOULD_RETRY_KEY, false)
        }
        if (jobId <= 0) return Result.success()

        val currRetryKey = sharedPref.getInt(StartExactJobReceiver.MAX_RETRY_KEY, maxRetryKey)
        val loadMovie = GlobalScope.async {
            loadReleaseMovie()
        }
        val loadTv = GlobalScope.async {
            loadReleaseTv()
        }
        while (loadTv.isActive || loadMovie.isActive) delay(600)
        val resTv = loadTv.getCompleted()
        val resMovie = loadMovie.getCompleted()
        if ((resTv == null || resMovie == null) && shouldRetryKey && currRetryKey > 0) {
            Log.d("ReminderRelease", "we should retry this job.... currentRetry is $currRetryKey")
            sharedPref.edit {
                putInt(StartExactJobReceiver.MAX_RETRY_KEY, currRetryKey - 1)
            }
            return Result.retry()
        }

        if (shouldRetryKey && currRetryKey <= 0)
            sharedPref.edit {
                remove(StartExactJobReceiver.MAX_RETRY_KEY)
            }
        sendNotification(resMovie, "Movie", PublicContract.ContentDisplayType.MOVIE, 0x22a)
        sendNotification(resTv, "Tv Shows", PublicContract.ContentDisplayType.TV_SHOWS, 0x1af)
        return Result.success()
    }

    private fun sendNotification(
        mainDataItemResponse: MainDataItemResponse?,
        typeStr: String,
        displayType: PublicContract.ContentDisplayType,
        notifId: Int
    ) {
        if (mainDataItemResponse?.contents != null) {
            val notifBuilder = NotificationCompat.Builder(applicationContext, NOTIF_CHANNEL_ID).apply {
                setSmallIcon(R.mipmap.ic_launcher)

                val nTitle = "${mainDataItemResponse.contents.size} $typeStr Released Today!"
                val nSummary = "Let's find out what of that!"
                setContentTitle(SpannableString(nTitle).also {
                    it[0 until nTitle.length] = RelativeSizeSpan(0.8f)
                })
                setContentText(nSummary)
                setLargeIcon(
                    BitmapFactory.decodeResource(
                        applicationContext.resources,
                        R.mipmap.ic_launcher
                    )
                )
                setStyle(NotificationCompat.InboxStyle().also {
                    for ((idx, mData) in mainDataItemResponse.contents.withIndex()) {
                        if (idx == 6) {
                            it.addLine(SpannableString("...${mainDataItemResponse.contents.size - idx} more").also { span ->
                                span[0..span.length] = RelativeSizeSpan(0.9f)
                            })

                        } else {
                            val titleCs = SpannableStringBuilder(mData.title)
                            titleCs[0..titleCs.length] = StyleSpan(Typeface.BOLD)
                            titleCs.append(" ${mData.overview}")
                            titleCs[0..titleCs.length] = RelativeSizeSpan(0.9f)
                            it.addLine(titleCs)
                        }
                    }
                    it.setBigContentTitle(nTitle)
                    it.setSummaryText(nSummary)
                    priority = NotificationCompat.PRIORITY_HIGH
                    setVibrate(longArrayOf(1000, 500, 1000, 1000))
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    setLights(Color.BLUE, 1000, 500)
                })

                val intentTo = Intent(applicationContext, MainTabUserActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(INTENT_FROM, FROM_REMINDER)
                    putExtra(NOTIF_ID, notifId)
                    putExtra(DISPLAY_TYPE, displayType.type)
                    putExtra(DISPLAY_CONTENT, mainDataItemResponse.fromDataIntoBundle())

                }

                setContentIntent(
                    PendingIntent.getActivity(
                        applicationContext, notifId, intentTo, PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
                setGroup(GROUP_KEY_NOTIF)
                setGroupSummary(true)
            }

            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        NOTIF_CHANNEL_ID,
                        "${NOTIF_CHANNEL_ID}$notifId",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
            manager.notify(notifId, notifBuilder.build())
        }
    }

    private fun loadReleaseMovie(): MainDataItemResponse? {
        val obj = GetObjectFromServer.getInstance(applicationContext)
        return obj.getSynchronousObj(
            GetMovies.getCurrentRelease(Calendar.getInstance()),
            MainDataItemResponse::class.java,
            "GetCurrentMovieRelease"
        )
    }

    private fun loadReleaseTv(): MainDataItemResponse? {
        val obj = GetObjectFromServer.getInstance(applicationContext)
        return obj.getSynchronousObj(
            GetTVShows.getCurrentRelease(Calendar.getInstance()),
            MainDataItemResponse::class.java,
            "GetCurrentTvRelease"
        )
    }
}