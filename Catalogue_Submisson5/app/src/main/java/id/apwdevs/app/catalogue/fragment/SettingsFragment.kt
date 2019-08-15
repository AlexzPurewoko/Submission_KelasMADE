package id.apwdevs.app.catalogue.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.annotation.StringRes
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import id.apwdevs.app.catalogue.R

class SettingsFragment : PreferenceFragmentCompat() {
    private var cardBgStatus: SwitchPreferenceCompat? = null
    private var cardBgMode: ListPreference? = null
    private var mDailyReminder: SwitchPreferenceCompat? = null
    private var mReleaseTodayReminder: SwitchPreferenceCompat? = null
    private var backdropCardPref: ListPreference? = null

    private var mDailyReminderTimePicker: Preference? = null
    private var mReleaseTodayTimePicker: Preference? = null
    private var mOnSettingsFragmentCallback: SettingCB? = null


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        cardBgMode = findPreference(R.string.card_bg_mode_key)
        cardBgStatus = findPreference(R.string.card_bg_status_key)
        backdropCardPref = findPreference(R.string.carddrop_w_key)
        cardBgStatus?.setOnPreferenceChangeListener { _, newValue ->
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

        mDailyReminder?.setOnPreferenceChangeListener { _, newValue ->
            val value = newValue as Boolean
            mDailyReminder?.setSummary(
                if (value) {
                    R.string.daily_reminder_summaryOn
                } else R.string.daily_reminder_summaryOff
            )
            mDailyReminder?.isChecked = value
            Handler().postDelayed({ update() }, 250)
            true
        }
        mReleaseTodayReminder?.setOnPreferenceChangeListener { _, newValue ->
            val value = newValue as Boolean
            mReleaseTodayReminder?.setSummary(
                if (value) {
                    R.string.release_today_reminder_summaryOn
                } else R.string.release_today_reminder_summaryOff
            )
            mReleaseTodayReminder?.isChecked = value
            Handler().postDelayed({ update() }, 250)
            true
        }
        implementTimePickerDialog()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mOnSettingsFragmentCallback = context as SettingCB
    }

    override fun onDetach() {
        super.onDetach()
        mOnSettingsFragmentCallback = null
    }

    fun update() {
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
            summary = ""
            summary = if (isEnabled) {
                getString(resWhenEnabled, arg)
            } else
                getString(resWhenDisabled)
        }
    }

    private fun showTimePickerDialog(preference: Preference) {
        mOnSettingsFragmentCallback?.onReqShowTimePickDialog(preference)
    }

    private fun <T : Preference> findPreference(@StringRes id: Int): T? = findPreference(getString(id))

    companion object {
        private var sInstance: SettingsFragment? = null
        @JvmStatic
        fun getInstance(): SettingsFragment {
            return sInstance ?: SettingsFragment().apply {
                sInstance = this
            }
        }
    }

    interface SettingCB {
        fun onReqShowTimePickDialog(preference: Preference)
    }
}