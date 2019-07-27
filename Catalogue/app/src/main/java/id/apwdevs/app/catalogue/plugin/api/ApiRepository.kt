package id.apwdevs.app.catalogue.plugin.api

import android.accounts.NetworkErrorException
import android.content.Context
import android.net.ConnectivityManager
import android.os.Parcelable
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import id.apwdevs.app.catalogue.plugin.view.ErrorSectionAdapter
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.net.HttpURLConnection
import java.net.UnknownHostException

@Deprecated(
    message = "You should use GetObjectFromServer instead as replacing of this class",
    level = DeprecationLevel.WARNING
)
class ApiRepository {
    fun doReqAndRetResponseAsync(
        context: Context?,
        url: String,
        tag: String,
        priority: Priority
    ): Deferred<ReturnedResults?> =
        GlobalScope.async {
            if (context != null && !isAnyAvailableNetworks(context))
                return@async ReturnedResults(
                    null,
                    RetError(
                        ErrorSectionAdapter.ERR_CODE_NO_NETWORK,
                        NetworkErrorException("Network is unactivated")
                    ),
                    false
                )
            var hasFinished = false
            var returnedResults: ReturnedResults? = null
            AndroidNetworking.get(url)
                .setTag(tag)
                .setPriority(priority)
                .build()
                .getAsString(object : StringRequestListener {
                    override fun onResponse(response: String?) {
                        returnedResults = ReturnedResults(
                            response,
                            null,
                            true
                        )
                        hasFinished = true
                    }

                    override fun onError(anError: ANError?) {
                        returnedResults = ReturnedResults(
                            null,
                            when (anError?.errorCode) {
                                HttpURLConnection.HTTP_NOT_FOUND -> RetError(
                                    ErrorSectionAdapter.ERR_NOT_FOUND,
                                    findSpecificCause(anError)
                                )
                                else -> {
                                    RetError(
                                        when (anError?.cause?.cause) {
                                            is UnknownHostException -> ErrorSectionAdapter.ERR_CODE_NET_FAILED
                                            else -> ErrorSectionAdapter.ERR_CODE_UNSPECIFIED
                                        },
                                        findSpecificCause(anError)
                                    )
                                }
                            }
                            ,
                            false
                        )
                        hasFinished = true
                        anError?.printStackTrace()
                    }

                })
            while (!hasFinished) {
                delay(1000)
            }
            returnedResults
        }

    data class ReturnedResults(
        val response: String?,
        val anErrorIfAny: RetError?,
        val isSuccess: Boolean
    )

    private fun findSpecificCause(e: Throwable?): Throwable? {
        if (e != null && e is ANError)
            findSpecificCause(e.cause)
        return e
    }

    private fun isAnyAvailableNetworks(context: Context): Boolean =
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo?.isConnected
            ?: false

    @Parcelize
    data class RetError(
        val errorCode: Int,
        val cause: Throwable?
    ) : Parcelable
}