package id.apwdevs.app.catalogue.plugin.api

import android.content.Context
import android.net.ConnectivityManager
import android.os.Parcelable
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.gsonparserfactory.GsonParserFactory
import com.androidnetworking.interfaces.ParsedRequestListener
import id.apwdevs.app.catalogue.plugin.view.ErrorSectionAdapter
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class GetObjectFromServer private constructor(appContext: Context) {
    private val weakContext: WeakReference<Context> = WeakReference(appContext)
    private val availNet: Boolean
        get() {
            return (weakContext.get()?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?)?.activeNetworkInfo?.isConnected
                ?: false
        }

    init {
        AndroidNetworking.setParserFactory(GsonParserFactory())
    }

    suspend fun <T> getObj(
        url: String,
        cls: Class<T>,
        tag: String,
        callbacks: GetObjectFromServerCallback<T>? = null,
        priority: Priority = Priority.LOW,
        connectTimeOut: Int = 15
    ) {

        var isFinished = false
        AndroidNetworking.get(url).apply {
            setTag(tag)
            setPriority(priority)
            setOkHttpClient(OkHttpClient.Builder().also {
                it.connectTimeout(connectTimeOut.toLong(), TimeUnit.SECONDS)
            }.build())
            if (!availNet)
                responseOnlyIfCached
        }.build().setDownloadProgressListener { bytesDownloaded, totalBytes ->
            if (weakContext.get() == null) {
                AndroidNetworking.cancel(tag)
                isFinished = true
            } else
                callbacks?.onProgress((bytesDownloaded * 100 / totalBytes).toDouble())
        }.getAsObject(cls, object : ParsedRequestListener<T> {
            override fun onResponse(response: T) {
                isFinished = true
                callbacks?.onSuccess(response)
            }

            override fun onError(anError: ANError?) {
                isFinished = true
                val retError = when (anError?.errorCode) {
                    HttpURLConnection.HTTP_NOT_FOUND -> RetError(
                        ErrorSectionAdapter.ERR_NOT_FOUND,
                        findSpecificCause(anError)
                    )
                    else -> {
                        RetError(
                            when (findSpecificCause(anError?.cause)) {
                                is UnknownHostException -> ErrorSectionAdapter.ERR_CODE_NET_FAILED
                                else -> ErrorSectionAdapter.ERR_CODE_UNSPECIFIED
                            },
                            findSpecificCause(anError)
                        )
                    }
                }
                callbacks?.onFailed(retError)

            }

        })

        while (!isFinished && weakContext.get() != null) {
            delay(500)
        }

    }

    private fun findSpecificCause(e: Throwable?): Throwable? {
        if (e != null && e is ANError)
            findSpecificCause(e.cause)
        return e
    }

    @Parcelize
    data class RetError(
        val errorCode: Int,
        val cause: Throwable?
    ) : Parcelable

    interface GetObjectFromServerCallback<T> {
        fun onSuccess(response: T)
        fun onFailed(retError: RetError)
        fun onProgress(percent: Double)
    }

    companion object {
        @Volatile
        private var thisInstance: GetObjectFromServer? = null

        @Synchronized
        fun getInstance(context: Context): GetObjectFromServer =
            thisInstance ?: GetObjectFromServer(context).also {
                thisInstance = it
            }
    }
}