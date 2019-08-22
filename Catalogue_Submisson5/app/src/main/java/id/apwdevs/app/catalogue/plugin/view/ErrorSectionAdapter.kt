package id.apwdevs.app.catalogue.plugin.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.plugin.gone
import id.apwdevs.app.catalogue.plugin.visible

class ErrorSectionAdapter(private val itemError: View) {
    private var imgError: ImageView
    private var errorTitle: TextView
    private var errorBody: TextView
    private var errorSuggestions: TextView
    private var errorMore: TextView
    private var errorSuggestionsTitle: TextView

    init {
        itemError.visibility = View.INVISIBLE
        imgError = itemError.findViewById(R.id.error_image)
        errorTitle = itemError.findViewById(R.id.error_title)
        errorBody = itemError.findViewById(R.id.error_body)
        errorSuggestions = itemError.findViewById(R.id.error_resolve_suggested)
        errorSuggestionsTitle = itemError.findViewById(R.id.error_text_suggest_title)
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
                errorSuggestionsTitle.visible()
            }
            ERR_CODE_NO_NETWORK -> {
                imgError.setImageResource(R.drawable.ic_signal_wifi_off_grey_24dp)
                errorTitle.text = ctx.getString(R.string.on_no_network)
                errorBody.text = ctx.getString(R.string.err_internet_unactivated)
                errorSuggestions.text = ctx.getString(R.string.msg_on_no_network)
                errorSuggestionsTitle.visible()
            }
            ERR_CODE_PARSE_FAILED -> {
                imgError.setImageResource(R.drawable.ic_parse_failed)
                errorTitle.text = ctx.getString(R.string.on_parse_failed)
                errorBody.text = ctx.getString(R.string.json_parser_error)
                errorSuggestions.text = ctx.getString(R.string.err_parse_suggestions)
                errorSuggestionsTitle.visible()
            }
            ERR_CODE_UNSPECIFIED -> {
                imgError.setImageResource(R.drawable.ic_parse_failed)
                errorTitle.text = ctx.getString(R.string.err_suspected)
                errorBody.text = retError.cause?.message
                errorSuggestions.text = ctx.getString(R.string.err_msg_unspecified_default)
                errorSuggestionsTitle.visible()
            }
            ERR_NOT_FOUND -> {
                imgError.setImageResource(R.drawable.ic_sentiment_dissatisfied_black_24dp)
                errorTitle.text = ctx.getString(R.string.err_not_found)
                errorBody.text = ctx.getString(R.string.err_not_found_message)
                errorSuggestions.text = ctx.getString(R.string.err_not_found_suggest)
                errorSuggestionsTitle.visible()
            }
            ERR_NO_RESULTS -> {
                imgError.setImageResource(R.drawable.ic_sentiment_dissatisfied_black_24dp)
                errorTitle.text = ctx.getString(R.string.err_no_results)
                errorBody.text = ""
                errorSuggestions.text = ctx.getString(R.string.err_no_results_suggest)
                errorSuggestionsTitle.visible()
            }
            ON_SEARCH_MODE -> {
                imgError.setImageResource(R.drawable.ic_search_black_24dp)
                errorTitle.text = ctx.getString(R.string.searching)
                errorBody.text = ctx.getString(R.string.wait_until_search_finished)
                errorSuggestions.text = ""
                errorSuggestionsTitle.gone()
                errorMore.gone()
                return
            }
            else -> return
        }
        errorMore.visible()
        errorMore.text =
            ctx.getString(R.string.caused_by, "${retError.cause?.javaClass?.simpleName}")
    }

    fun unDisplayError() {
        itemError.visibility = View.INVISIBLE
    }

    companion object {
        const val ON_SEARCH_MODE = 0xa22a
        const val ERR_NOT_FOUND = 0x5af
        const val ERR_CODE_PARSE_FAILED = 0xfa2
        const val ERR_CODE_NO_NETWORK = 0xaaf
        const val ERR_CODE_NET_FAILED = 0x3a
        const val ERR_CODE_UNSPECIFIED = 0x4af
        const val ERR_NO_RESULTS: Int = 0x44a

    }
}