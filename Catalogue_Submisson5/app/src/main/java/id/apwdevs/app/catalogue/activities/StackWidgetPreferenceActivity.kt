package id.apwdevs.app.catalogue.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.widget.FavoriteWidget
import kotlinx.android.synthetic.main.activity_stack_widget_preference.*

class StackWidgetPreferenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stack_widget_preference)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val appWidgetId =
            intent?.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
                ?: AppWidgetManager.INVALID_APPWIDGET_ID
        val ownUpdate = intent?.extras?.getBoolean(OWN_UPDATE, false) ?: false
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            AlertDialog.Builder(this).apply {
                title = getString(R.string.alert)
                setMessage(getString(R.string.appwidget_invalid_id_message))
                setPositiveButton(R.string.okay) { dialog, _ ->
                    dialog.dismiss()
                    setResult(Activity.RESULT_CANCELED, resultValue)
                    finish()
                }
            }.show()
        }
        //load defaults if any

        radioGroup_content_types.check(
            when (getSharedPreferences(
                PublicContract.WIDGET_SHARED_PREFERENCES,
                Context.MODE_PRIVATE
            ).getInt("widget_conf_${appWidgetId}_type", -1)) {
                PublicContract.ContentDisplayType.MOVIE.type -> R.id.radio_favorite_movie
                PublicContract.ContentDisplayType.TV_SHOWS.type -> R.id.radio_favorite_tv
                else -> -1
            }
        )
        btn_save.setOnClickListener {
            when (radioGroup_content_types.checkedRadioButtonId) {
                R.id.radio_favorite_movie -> {
                    save(
                        appWidgetId,
                        PublicContract.ContentDisplayType.MOVIE,
                        resultValue,
                        ownUpdate
                    )
                }
                R.id.radio_favorite_tv -> {
                    save(
                        appWidgetId,
                        PublicContract.ContentDisplayType.TV_SHOWS,
                        resultValue,
                        ownUpdate
                    )
                }
                else -> showExitToast()
            }
        }
    }

    override fun onBackPressed() {
        showExitToast()
    }

    private fun showExitToast() {
        Toast.makeText(this, getString(R.string.appwidget_not_right_value), Toast.LENGTH_LONG)
            .show()
    }

    private fun save(
        appWidgetId: Int,
        contentDisplayType: PublicContract.ContentDisplayType,
        resultIntent: Intent,
        ownUpdate: Boolean
    ) {
        getSharedPreferences(PublicContract.WIDGET_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit(
            commit = true
        ) {
            putInt("widget_conf_${appWidgetId}_type", contentDisplayType.type)
        }
        FavoriteWidget.updateAppWidget(this, AppWidgetManager.getInstance(this), appWidgetId)
        if (ownUpdate) {
            AppWidgetManager.getInstance(this)
                .notifyAppWidgetViewDataChanged(appWidgetId, R.id.stackview_widget)
            finish()
            return
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    companion object {
        const val OWN_UPDATE = "OWN_UPDATE"
    }
}
