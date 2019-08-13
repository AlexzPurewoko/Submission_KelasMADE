package id.apwdevs.app.catalogue.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.activities.MainTabUserActivity
import java.util.*

class DailyReminderNotif(context: Context, jobParams: WorkerParameters) : Worker(context, jobParams) {
    companion object {
        const val INTENT_FROM_DAILY_REMINDER: Int = 0xffaa
        const val NOTIF_ID = 100
    }

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
                    applicationContext, NOTIF_ID, Intent(
                        applicationContext,
                        MainTabUserActivity::class.java
                    ).apply {
                        putExtra(ReleaseTodayReminder.INTENT_FROM, INTENT_FROM_DAILY_REMINDER)
                        putExtra(ReleaseTodayReminder.NOTIF_ID, NOTIF_ID)
                    }, PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            priority = NotificationCompat.PRIORITY_HIGH
            setVibrate(longArrayOf(1000, 500, 500, 1000))
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            setLights(Color.RED, 1000, 500)
        }

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "Daily Reminder Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        manager.notify(NOTIF_ID, notifBuilder.build())
    }
}