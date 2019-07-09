package id.apwdevs.app.catalogue.plugin.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.plugin.api.ApiRepository

class ErrorSectionAdapter(private val itemError: View) {
    private var imgError : ImageView
    private var errorTitle : TextView
    private var errorBody : TextView
    init {
        itemError.visibility = View.INVISIBLE
        imgError = itemError.findViewById(R.id.error_image)
        errorTitle = itemError.findViewById(R.id.error_title)
        errorBody = itemError.findViewById(R.id.error_body)
    }

    //@SuppressLint("SetTextI18n")
    fun displayError(retError: ApiRepository.RetError){
        itemError.visibility = View.VISIBLE
        val ctx = itemError.context
        when(retError.errorCode){
            ERR_CODE_NET_FAILED -> {
                imgError.setImageResource(R.drawable.ic_signal_wifi_off_grey_24dp)
                errorTitle.text = ctx.getString(R.string.on_network_failed)
                errorBody.text = ctx.getString(R.string.msg_on_net_error)
                errorBody.append("\nCaused by : ${retError.cause?.javaClass?.simpleName}")
            }
            ERR_CODE_NO_NETWORK -> {
                imgError.setImageResource(R.drawable.ic_signal_wifi_off_grey_24dp)
                errorTitle.text = ctx.getString(R.string.on_no_network)
                errorBody.text = ctx.getString(R.string.msg_on_no_network)
                errorBody.append("\nCaused by : ${retError.cause?.javaClass?.simpleName}")
            }
            ERR_CODE_PARSE_FAILED -> {
                imgError.setImageResource(R.drawable.ic_parse_failed)
                errorTitle.text = ctx.getString(R.string.on_parse_failed)
                
                //override from cause Exception
                errorBody.text = retError.cause?.localizedMessage
                errorBody.append("\nCaused by : ${retError.cause?.javaClass?.simpleName}")
            }
            ERR_CODE_UNSPECIFIED -> {
                imgError.setImageResource(R.drawable.ic_parse_failed)

                errorTitle.text = ctx.getString(R.string.err_suspected)
                // if error is specified, then will override into caused exception
                errorBody.text = ctx.getString(R.string.err_msg_unspecified_default)
                errorBody.append("\nCaused by : ${retError.cause?.javaClass?.simpleName}")
                retError.cause?.let { 
                    errorBody.text = it.message
                    errorBody.append("\nCaused by : ${it.javaClass.simpleName}")
                    return
                }
            }
        }
    }

    fun unDisplayError(){
        itemError.visibility = View.INVISIBLE
    }

    companion object {
        const val ERR_CODE_PARSE_FAILED = 0xfa2
        const val ERR_CODE_NO_NETWORK = 0xaaf
        const val ERR_CODE_NET_FAILED = 0x3a
        const val ERR_CODE_UNSPECIFIED = 0x4af

    }
}