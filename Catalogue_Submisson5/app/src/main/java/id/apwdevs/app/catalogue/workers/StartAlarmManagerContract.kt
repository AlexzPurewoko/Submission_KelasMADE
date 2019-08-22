package id.apwdevs.app.catalogue.workers

import id.apwdevs.app.catalogue.R

object StartAlarmManagerContract {

    internal val listPendingAlarmJob = listOf(
        AlarmIdentity(
            0,
            "7:00",
            "HH:mm",
            true,
            "daily_reminder_time",
            true,
            R.string.daily_reminder_key,
            true
        ),
        AlarmIdentity(
            1,
            "8:00",
            "HH:mm",
            true,
            "release_today_reminder_time",
            true,
            R.string.release_today_reminder_key,
            true
        )
    )

    data class AlarmIdentity(
        val lJobId: Int,
        val date: String,
        val dateFormat: String,
        val useTimeSharedPreference: Boolean,
        val timeSharedPrefName: String?,
        val useSharedPrefControls: Boolean,
        val sharedPrefControlsName: Int,
        val sharedPrefControlsDefault: Boolean,
        val allowNextReschedule: Boolean = true
    )
}