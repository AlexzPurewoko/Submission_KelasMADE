package id.apwdevs.moTvCatalogue.plugin.api

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class ApiRepository {
    fun doRequestAndReturnJSON(url: String, tag: String, priority: Priority): Deferred<ReturnedResults?> =
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
                            anError,
                            false
                        )
                        hasFinished = true
                    }

                })
            while (!hasFinished) {
            }
            returnedResults
        }

    data class ReturnedResults(
        val response: String?,
        val anErrorIfAny: ANError?,
        val isSuccess: Boolean
    )
}