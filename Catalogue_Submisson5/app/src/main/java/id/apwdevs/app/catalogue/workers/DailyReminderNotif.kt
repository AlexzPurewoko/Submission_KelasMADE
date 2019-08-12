package id.apwdevs.app.catalogue.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.activities.MainTabUserActivity
import java.util.*

class DailyReminderNotif(context: Context, jobParams: WorkerParameters) : Worker(context, jobParams) {
    override fun doWork(): Result {
        sendNotification()
        return Result.success()
    }

    private fun sendNotification() {
        val channelId = "Daily Reminder"
        val rand = Random().nextInt(3)

        val nTitle = applicationContext.resources.getStringArray(R.array.daily_reminder_notiftitle)
        val nContent = applicationContext.resources.getStringArray(R.array.daily_reminder_notifcontent)
        val notifBuilder = NotificationCompat.Builder(applicationContext, channelId).apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle(nTitle[rand])
            setContentText(nContent[rand])
            setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources, R.mipmap.ic_launcher))
            setContentIntent(
                PendingIntent.getActivity(
                    applicationContext, 0, Intent(
                        applicationContext,
                        MainTabUserActivity::class.java
                    ), 0
                )
            )
        }

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "MCobaNotifHEllo",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        manager.notify(100, notifBuilder.build())
    }
}