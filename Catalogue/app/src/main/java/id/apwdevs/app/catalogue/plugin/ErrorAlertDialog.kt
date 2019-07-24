package id.apwdevs.app.catalogue.plugin

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.plugin.api.ApiRepository
import id.apwdevs.app.catalogue.plugin.view.ErrorSectionAdapter

class ErrorAlertDialog : DialogFragment() {

    private var onListener: OnErrorDialogBtnClickListener? = null
    var returnedError: ApiRepository.RetError? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext()).apply {

            // if savedInstanceState is not null, i will restore back again its error data
            savedInstanceState?.let {
                returnedError = it.getParcelable(EXTRA_ERROR_DATA)
            }

            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.error_layout, requireActivity().window.decorView as ViewGroup, false)
            setView(view)

            ErrorSectionAdapter(view).displayError(returnedError)
            setCancelable(false)
            setNegativeButton(getString(R.string.go_back)) { _, _ ->
                onListener?.onRequestBack(this@ErrorAlertDialog)
            }
            setPositiveButton(getString(R.string.refresh)) { _, _ ->
                onListener?.onRequestRefresh(this@ErrorAlertDialog)
            }
        }.create()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_ERROR_DATA, returnedError)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onListener = context as OnErrorDialogBtnClickListener
        } catch (e: ClassCastException) {
            Log.e("CastException", "You must implement the OnErrorDialogBtnClickListener callbacks in your class")
        }
    }

    override fun onDetach() {
        super.onDetach()
        onListener = null
    }

    interface OnErrorDialogBtnClickListener {
        fun onRequestRefresh(errorAlertDialog: ErrorAlertDialog)
        fun onRequestBack(errorAlertDialog: ErrorAlertDialog)
    }

    companion object {
        private const val EXTRA_ERROR_DATA: String = "EXTRA_DATA"
    }
}