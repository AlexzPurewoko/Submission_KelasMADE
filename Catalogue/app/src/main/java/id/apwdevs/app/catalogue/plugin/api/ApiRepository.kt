package id.apwdevs.app.catalogue.plugin.api

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import id.apwdevs.app.catalogue.plugin.view.ErrorSectionAdapter
import kotlinx.coroutines.*
import java.net.UnknownHostException

class ApiRepository {

    suspend fun doReqandRetResponse(url: String, tag: String, priority: Priority): ReturnedResults? =
        withContext(Dispatchers.Default) {
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
                            RetError(
                                when (anError?.cause?.cause) {
                                    is UnknownHostException -> ErrorSectionAdapter.ERR_CODE_NET_FAILED
                                    else -> ErrorSectionAdapter.ERR_CODE_UNSPECIFIED
                                },
                                anError?.cause?.cause
                            ),
                            false
                        )
                        hasFinished = true
                    }

                })

            while (!hasFinished) {
            }
            returnedResults
        }

    fun doReqAndRetResponseAsync(url: String, tag: String, priority: Priority): Deferred<ReturnedResults?> =
        GlobalScope.async {
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
                            RetError(
                                when (anError?.cause?.cause) {
                                    is UnknownHostException -> ErrorSectionAdapter.ERR_CODE_NET_FAILED
                                    else -> ErrorSectionAdapter.ERR_CODE_UNSPECIFIED
                                },
                                anError?.cause?.cause
                            ),
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

    data class RetError(
        val errorCode: Int,
        val cause: Throwable?
    )
}