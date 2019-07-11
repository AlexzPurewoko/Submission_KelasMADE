package id.apwdevs.app.catalogue.plugin

import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import id.apwdevs.app.catalogue.R

class ProgressDialog(context: Context) {
    private var textTitle: TextView

    private var alertDialog: AlertDialog
    private var rootView: RelativeLayout

    init {
        alertDialog = AlertDialog.Builder(context).create().apply {
            rootView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null, false) as RelativeLayout
            textTitle = rootView.findViewById(R.id.progress_title)
            setView(rootView)
        }
    }

    fun show() {
        alertDialog.show()
    }

    fun dismiss() {
        alertDialog.dismiss()
    }
}