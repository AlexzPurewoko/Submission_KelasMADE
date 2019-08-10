package id.apwdevs.app.catalogue.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import id.apwdevs.app.catalogue.activities.DetailActivity
import id.apwdevs.app.catalogue.activities.MainTabUserActivity
import id.apwdevs.app.catalogue.database.FavoriteDatabase
import id.apwdevs.app.catalogue.plugin.PublicContract
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LaunchReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_LAUNCH_MAIN = "id.apwdevs.app.catalogue.LAUNCH_MAIN_ACTIVITY"
        const val ACTION_LAUNCH_DETAIL = "id.apwdevs.app.catalogue.LAUNCH_DETAIL_ACTIVITY"
        const val EXTRA_ID = "EXTRA_CONTENT_ID"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        when (intent.action) {
            ACTION_LAUNCH_MAIN -> {
                context.startActivity(
                    Intent(context, MainTabUserActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
            }
            ACTION_LAUNCH_DETAIL -> {
                GlobalScope.launch {
                    val cId = intent.getIntExtra(EXTRA_ID, -1)
                    if (cId == -1) return@launch
                    val favDao = FavoriteDatabase.getInstance(context).favoriteDao()
                    favDao.getItemAt(cId)?.let {
                        context.startActivity(
                            Intent(context, DetailActivity::class.java).apply {
                                putExtras(Bundle().also { mExtra ->
                                    mExtra.putParcelable(
                                        DetailActivity.EXTRA_DETAIL_TYPES,
                                        PublicContract.ContentDisplayType.FAVORITES
                                    )
                                    mExtra.putInt(DetailActivity.EXTRA_ID, it.id)
                                    mExtra.putParcelable(DetailActivity.EXTRA_CONTENT_DETAILS, it)
                                })
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        )


                    }
                }

            }
        }


    }
}
