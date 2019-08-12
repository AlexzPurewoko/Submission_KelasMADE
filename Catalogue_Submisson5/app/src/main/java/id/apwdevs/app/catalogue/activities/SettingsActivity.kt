package id.apwdevs.app.catalogue.activities

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import id.apwdevs.app.catalogue.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private var cardBgStatus: SwitchPreferenceCompat? = null
        private var cardBgMode: ListPreference? = null
        private var mDailyReminder: SwitchPreferenceCompat? = null
        private var mReleaseTodayReminder: SwitchPreferenceCompat? = null
        private var backdropCardPref: ListPreference? = null
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
            mDailyReminder?.setOnPreferenceChangeListener { preference, newValue ->
                val value = newValue as Boolean
                mDailyReminder?.setSummary(
                    if (value) {
                        R.string.daily_reminder_summaryOn
                    } else R.string.daily_reminder_summaryOff
                )
                value
            }
            mReleaseTodayReminder?.setOnPreferenceChangeListener { preference, newValue ->
                val value = newValue as Boolean
                mReleaseTodayReminder?.setSummary(
                    if (value) {
                        R.string.release_today_reminder_summaryOn
                    } else R.string.release_today_reminder_summaryOff
                )
                value
            }
        }

        private fun <T : Preference> findPreference(@StringRes id: Int): T? = findPreference(getString(id))

    }
}