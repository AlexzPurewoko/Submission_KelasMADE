package id.apwdevs.app.catalogue.plugin

import android.database.ContentObserver
import android.os.Handler

class DataObserver(handler: Handler, private val forceLoad: (selfChange: Boolean) -> Unit) : ContentObserver(handler) {
    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        forceLoad(selfChange)
    }
}