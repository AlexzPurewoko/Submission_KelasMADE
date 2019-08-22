package id.apwdevs.app.catalogue.activities

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.fragment.SettingsFragment
import id.apwdevs.app.catalogue.plugin.ApplyLanguage
import id.apwdevs.app.catalogue.plugin.PublicContract
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity(), SettingsFragment.SettingCB {
    private lateinit var showDialogPick: AlertDialog
    private lateinit var mViewModel: SettingViewModel
    private val isModified: Int
        get() {
            var isMod = Activity.RESULT_CANCELED
            supportFragmentManager.fragments.forEach {
                if (it is GetValues)
                    isMod = it.isHasBeenModified()
            }
            return isMod
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(SettingViewModel::class.java)
        setUpDialog()
    }

    private fun setUpDialog() {
        showDialogPick = AlertDialog.Builder(this).apply {
            val timePicker = TimePicker(context).also { picker ->
                picker.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            val layout = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL

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
            }
            setView(layout)
            setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton(getString(R.string.set)) { dialog, _ ->
                mViewModel.mCurrentPreference.value?.let {
                    it.sharedPreferences.edit(true) {
                        putString(it.key, getTimePickerText(timePicker))
                    }
                }
                dialog.dismiss()
                onUpdate()
            }

            mViewModel.mCurrentPreference.observe(this@SettingsActivity, androidx.lifecycle.Observer {
                val defaultVal = it?.sharedPreferences?.getString(it.key, getTimePickerText(timePicker))
                Calendar.getInstance().apply {
                    time = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(defaultVal)
                    setTimePickerTime(
                        timePicker,
                        get(Calendar.HOUR_OF_DAY),
                        get(Calendar.MINUTE)
                    )
                }
            })
        }.create()
    }

    private fun onUpdate() {
        supportFragmentManager.fragments.forEach {
            if (it is SettingsFragment) {
                it.update()
            }
        }
    }

    override fun onBackPressed() {
        backActivity()
    }

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let {
            var newCtx: Context?
            it.getSharedPreferences(PublicContract.SHARED_PREF_GLOBAL_NAME, Context.MODE_PRIVATE)
                .apply {
                    newCtx = when (getString("app_languages", "system")) {
                        "force_en" -> ApplyLanguage.wrap(it, Locale("en"))
                        "force_in" -> ApplyLanguage.wrap(it, Locale("in"))
                        else -> {
                            super.attachBaseContext(newBase)
                            return
                        }
                    }
                    super.attachBaseContext(newCtx)
                }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            backActivity()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun backActivity() {
        setResult(
            isModified
        )
        finish()
    }

    override fun onReqShowTimePickDialog(preference: Preference) {
        mViewModel.mCurrentPreference.value = preference
        showDialogPick.show()
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

}

internal interface GetValues {
    fun isHasBeenModified(): Int
}

class SettingViewModel : ViewModel() {
    var mCurrentPreference: MutableLiveData<Preference> = MutableLiveData()
}