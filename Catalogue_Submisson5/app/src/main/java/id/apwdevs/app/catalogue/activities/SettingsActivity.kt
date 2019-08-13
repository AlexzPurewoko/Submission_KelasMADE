package id.apwdevs.app.catalogue.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TimePicker
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.workers.ReleaseTodayReminder
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity() {
    companion object {
        const val INTENT_FROM_SETTINGS_ACTIVITY: Int = 0xaca32
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {
        backActivity()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            backActivity()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun backActivity() {
        startActivity(Intent(this, MainTabUserActivity::class.java).apply {
            putExtra(ReleaseTodayReminder.INTENT_FROM, INTENT_FROM_SETTINGS_ACTIVITY)
        })
    }


    class SettingsFragment : PreferenceFragmentCompat() {
        private var cardBgStatus: SwitchPreferenceCompat? = null
        private var cardBgMode: ListPreference? = null
        private var mDailyReminder: SwitchPreferenceCompat? = null
        private var mReleaseTodayReminder: SwitchPreferenceCompat? = null
        private var backdropCardPref: ListPreference? = null

        private var mDailyReminderTimePicker: Preference? = null
        private var mReleaseTodayTimePicker: Preference? = null
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            cardBgMode = findPreference(R.string.card_bg_mode_key)
            cardBgStatus = findPreference(R.string.card_bg_status_key)
            backdropCardPref = findPreference(R.string.carddrop_w_key)
            cardBgStatus?.setOnPreferenceChangeListener { preference, newValue ->
                val value = newValue as Boolean
                cardBgMode?.isEnabled = value
                cardBgStatus?.isChecked = value
                backdropCardPref?.isEnabled = value

                value
            }
            val checked = cardBgStatus?.isChecked ?: false
            cardBgMode?.isEnabled = checked
            backdropCardPref?.isEnabled = checked

            mDailyReminder = findPreference(R.string.daily_reminder_key)
            mReleaseTodayReminder = findPreference(R.string.release_today_reminder_key)
            mDailyReminderTimePicker = findPreference(R.string.daily_reminder_time_key)
            mReleaseTodayTimePicker = findPreference(R.string.release_today_reminder_time_key)

            mDailyReminder?.setOnPreferenceChangeListener { preference, newValue ->
                val value = newValue as Boolean
                mDailyReminder?.setSummary(
                    if (value) {
                        R.string.daily_reminder_summaryOn
                    } else R.string.daily_reminder_summaryOff
                )
                mDailyReminder?.isChecked = value
                update()
                true
            }
            mReleaseTodayReminder?.setOnPreferenceChangeListener { preference, newValue ->
                val value = newValue as Boolean
                mReleaseTodayReminder?.setSummary(
                    if (value) {
                        R.string.release_today_reminder_summaryOn
                    } else R.string.release_today_reminder_summaryOff
                )
                mReleaseTodayReminder?.isChecked = value
                update()
                true
            }
            implementTimePickerDialog()
        }

        private fun update() {
            setSummaryOnPref(
                mDailyReminderTimePicker,
                mDailyReminder?.isChecked ?: false,
                R.string.daily_reminder_time_summaryOn,
                R.string.daily_reminder_time_summaryOff,
                mDailyReminderTimePicker?.let {
                    return@let it.sharedPreferences.getString(it.key, "00:00") ?: "00:00"
                } ?: "00:00")

            setSummaryOnPref(
                mReleaseTodayTimePicker,
                mReleaseTodayReminder?.isChecked ?: false,
                R.string.release_today_reminder_time_summaryOn,
                R.string.release_today_reminder_time_summaryOff,
                mReleaseTodayTimePicker?.let {
                    return@let it.sharedPreferences.getString(it.key, "00:00") ?: "00:00"
                } ?: "00:00")
        }

        private fun implementTimePickerDialog() {
            mDailyReminderTimePicker?.setOnPreferenceClickListener {
                showTimePickerDialog(it)
                true
            }
            mReleaseTodayTimePicker?.setOnPreferenceClickListener {
                showTimePickerDialog(it)
                true
            }
            update()

        }

        private fun setSummaryOnPref(
            preference: Preference?,
            isEnabled: Boolean,
            resWhenEnabled: Int,
            resWhenDisabled: Int,
            arg: String
        ) {
            preference?.apply {
                this.isEnabled = isEnabled
                summary = if (isEnabled) {
                    getString(resWhenEnabled, arg)
                } else
                    getString(resWhenDisabled)
            }
        }

        private fun showTimePickerDialog(preference: Preference) {
            AlertDialog.Builder(requireContext()).apply {

                val timePicker: TimePicker
                setView(LinearLayout(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.VERTICAL
                    timePicker = TimePicker(context).also { picker ->
                        picker.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }
                    addView(timePicker)
                    addView(CheckBox(context).also { checkBox ->
                        checkBox.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).also {
                            it.setMargins(8, 8, 8, 8)
                        }
                        checkBox.text = context.getString(R.string.time_picker_allow_24)
                        checkBox.setOnCheckedChangeListener { _, isChecked ->
                            timePicker.setIs24HourView(isChecked)
                        }
                        timePicker.setIs24HourView(checkBox.isChecked)
                    })
                })
                setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                setPositiveButton("Set") { dialog, _ ->
                    preference.sharedPreferences.edit {
                        putString(preference.key, getTimePickerText(timePicker))
                    }
                    dialog.dismiss()
                }

                // sets the configuration
                val defaultVal = preference.sharedPreferences.getString(preference.key, getTimePickerText(timePicker))
                Calendar.getInstance().apply {
                    time = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(defaultVal)
                    setTimePickerTime(
                        timePicker,
                        get(Calendar.HOUR_OF_DAY),
                        get(Calendar.MINUTE)
                    )
                }
            }.show()
        }

        @Suppress("DEPRECATION")
        private fun getTimePickerText(timePicker: TimePicker): String {
            val hour: Int
            val minute: Int

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hour = timePicker.hour
                minute = timePicker.minute
            } else {
                hour = timePicker.currentHour
                minute = timePicker.currentMinute
            }
            return "$hour:$minute"
        }

        @Suppress("DEPRECATION")
        private fun setTimePickerTime(timePicker: TimePicker, hour: Int, minute: Int) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.hour = hour
                timePicker.minute = minute
            } else {
                timePicker.currentHour = hour
                timePicker.currentMinute = minute
            }
        }

        private fun <T : Preference> findPreference(@StringRes id: Int): T? = findPreference(getString(id))

    }

}