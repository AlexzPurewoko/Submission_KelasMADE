package id.apwdevs.app.catalogue.plugin.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer

class ErrorSectionAdapter(private val itemError: View) {
    private var imgError: ImageView
    private var errorTitle: TextView
    private var errorBody: TextView
    private var errorSuggestions: TextView
    private var errorMore: TextView

    init {
        itemError.visibility = View.INVISIBLE
        imgError = itemError.findViewById(R.id.error_image)
        errorTitle = itemError.findViewById(R.id.error_title)
        errorBody = itemError.findViewById(R.id.error_body)
        errorSuggestions = itemError.findViewById(R.id.error_resolve_suggested)
        errorMore = itemError.findViewById(R.id.error_resolve_more)
    }

    fun displayError(retError: GetObjectFromServer.RetError?) {
        if (retError == null) return
        itemError.visibility = View.VISIBLE
        val ctx = itemError.context
        when (retError.errorCode) {
            ERR_CODE_NET_FAILED -> {
                imgError.setImageResource(R.drawable.ic_signal_wifi_off_grey_24dp)
                errorTitle.text = ctx.getString(R.string.on_network_failed)
                errorBody.text = ctx.getString(R.string.net_failed_error)
                errorSuggestions.text = ctx.getString(R.string.msg_on_net_error)

            }
            ERR_CODE_NO_NETWORK -> {
                imgError.setImageResource(R.drawable.ic_signal_wifi_off_grey_24dp)
                errorTitle.text = ctx.getString(R.string.on_no_network)
                errorBody.text = ctx.getString(R.string.err_internet_unactivated)
                errorSuggestions.text = ctx.getString(R.string.msg_on_no_network)
            }
            ERR_CODE_PARSE_FAILED -> {
                imgError.setImageResource(R.drawable.ic_parse_failed)
                errorTitle.text = ctx.getString(R.string.on_parse_failed)
                errorBody.text = ctx.getString(R.string.json_parser_error)
                errorSuggestions.text = ctx.getString(R.string.err_parse_suggestions)
            }
            ERR_CODE_UNSPECIFIED -> {
                imgError.setImageResource(R.drawable.ic_parse_failed)
                errorTitle.text = ctx.getString(R.string.err_suspected)
                errorBody.text = retError.cause?.message
                errorSuggestions.text = ctx.getString(R.string.err_msg_unspecified_default)
            }
            ERR_NOT_FOUND -> {
                imgError.setImageResource(R.drawable.ic_sentiment_dissatisfied_black_24dp)
                errorTitle.text = ctx.getString(R.string.err_not_found)
                errorBody.text = ctx.getString(R.string.err_not_found_message)
                errorSuggestions.text = ctx.getString(R.string.err_not_found_suggest)
            }
            else -> return
        }
        errorMore.text = ctx.getString(R.string.caused_by, "${retError.cause?.javaClass?.simpleName}")
    }

    fun unDisplayError() {
        itemError.visibility = View.INVISIBLE
    }

    companion object {
        const val ERR_NOT_FOUND = 0x5af
        const val ERR_CODE_PARSE_FAILED = 0xfa2
        const val ERR_CODE_NO_NETWORK = 0xaaf
        const val ERR_CODE_NET_FAILED = 0x3a
        const val ERR_CODE_UNSPECIFIED = 0x4af

    }
}