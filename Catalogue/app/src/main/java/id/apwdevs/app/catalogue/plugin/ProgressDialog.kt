package id.apwdevs.app.catalogue.plugin

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import id.apwdevs.app.catalogue.R

class ProgressDialog(private val context: Context) {
    private lateinit var textTitle: TextView
    private lateinit var alertDialog: AlertDialog
    private lateinit var rootView: RelativeLayout

    var text: CharSequence? = null
        set(value) {
            textTitle.text = value
            field = value
        }

    init {
        setDialog()
    }

    @SuppressLint("InflateParams")
    private fun setDialog() {
        alertDialog = AlertDialog.Builder(context).create().apply {
            rootView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null, false) as RelativeLayout
            textTitle = rootView.findViewById(R.id.progress_title)
            setView(rootView)
            setCancelable(false)
        }
    }

    fun show() {
        alertDialog.show()
    }

    fun dismiss() {
        alertDialog.dismiss()
    }
}