package id.apwdevs.app.catalogue.plugin

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.*

class TimePickerPreference : androidx.preference.PreferenceDialogFragmentCompat() {

    private var lastHour: Int = 0
    private var lastMinute: Int = 0
    private lateinit var timePicker: TimePicker

    init {
        initBtn()
    }

    private fun initBtn() {
        Calendar.getInstance().apply {
            lastHour = get(Calendar.HOUR_OF_DAY)
            lastMinute = get(Calendar.MINUTE)
        }
        preference.positiveButtonText = "Set"
        preference.negativeButtonText = "Cancel"
    }

    override fun onCreateDialogView(context: Context?): View {
        timePicker = TimePicker(context)
        return timePicker
    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.hour = lastHour
            timePicker.minute = lastMinute
        } else {
            timePicker.currentHour = lastHour
            timePicker.currentMinute = lastMinute
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                lastHour = timePicker.hour
                lastMinute = timePicker.minute
            } else {
                lastHour = timePicker.currentHour
                lastMinute = timePicker.currentMinute
            }
            val time = "$lastHour:$lastMinute"
            if (preference.callChangeListener(time))
                preference.persistStringSet(setOf(time))
        }
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
        builder?.apply {
            timePicker = TimePicker(context)
            timePicker.setIs24HourView(true)
            setView(timePicker)
            val nTime = preference.sharedPreferences.getString(preference.key, "00:00")
            Calendar.getInstance().apply {
                this.time = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(nTime)
                lastMinute = get(Calendar.MINUTE)
                lastHour = get(Calendar.HOUR)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.hour = lastHour
                timePicker.minute = lastMinute
            } else {
                timePicker.currentHour = lastHour
                timePicker.currentMinute = lastMinute
            }

        }
    }
}