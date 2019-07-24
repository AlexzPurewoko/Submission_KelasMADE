package id.apwdevs.app.catalogue.plugin

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import id.apwdevs.app.catalogue.R

class ProgressDialog : DialogFragment() {
    private lateinit var textTitle: TextView
    private lateinit var progressBar: ProgressBar
    var text: CharSequence? = null
        set(value) {
            textTitle.text = value
            field = value
        }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext()).apply {
            val v = LayoutInflater.from(requireContext()).inflate(R.layout.progress_dialog, null, false)
            setView(v)
            textTitle = v.findViewById(R.id.progress_title)
            progressBar = v.findViewById(R.id.progressBar)
            text = getString(R.string.dialog_message)
            setCancelable(false)
        }.create()

    override fun onDismiss(dialog: DialogInterface) {
        progressBar.isIndeterminate = false
        super.onDismiss(dialog)
    }

    override fun onResume() {
        super.onResume()
        progressBar.isIndeterminate = true
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }
}