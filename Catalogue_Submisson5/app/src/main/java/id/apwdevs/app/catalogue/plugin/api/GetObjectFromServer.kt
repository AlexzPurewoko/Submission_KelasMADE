package id.apwdevs.app.catalogue.plugin.api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.net.ConnectivityManager
import android.os.Parcelable
import android.util.Log
import android.widget.ImageView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.ANRequest
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.gsonparserfactory.GsonParserFactory
import com.androidnetworking.interfaces.BitmapRequestListener
import com.androidnetworking.interfaces.ParsedRequestListener
import com.androidnetworking.interfaces.StringRequestListener
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

    private fun getAndroidNet(
        url: String,
        tag: String,
        priority: Priority,
        connectTimeOut: Int,
        forceLoadFromCache: Boolean = false
    ): ANRequest<*> {
        return AndroidNetworking.get(url).apply {
            setTag(tag)
            setPriority(priority)
            setOkHttpClient(OkHttpClient.Builder().also {
                it.connectTimeout(connectTimeOut.toLong(), TimeUnit.SECONDS)
            }.build())
            if (!availNet || forceLoadFromCache)
                responseOnlyIfCached
        }.build()
    }

    fun getBitmapNoProgress(
        size: Point,
        posterPath: String,
        enableScaling: Boolean = false,
        scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_XY,
        forceLoadFromCache: Boolean = false,
        response: (response: Bitmap?) -> Unit
    ) {
        AndroidNetworking.get(GetImageFiles.getImg(size.x, posterPath)).apply {
            setPriority(Priority.LOW)
            setTag("")
            if (enableScaling) {
                setBitmapMaxHeight(size.y)
                setBitmapMaxWidth(size.x)
                setImageScaleType(scaleType)
            }
            if (!availNet || forceLoadFromCache)
                responseOnlyIfCached
        }.build()
            .getAsBitmap(object : BitmapRequestListener {
                override fun onResponse(response: Bitmap?) {
                    response(response)
                }

                override fun onError(anError: ANError?) {
                    Log.e("ErrorDisplayBitmap", anError?.errorBody, anError)
                }

            })
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
        getAndroidNet(
            url,
            tag,
            priority,
            connectTimeOut
        ).setDownloadProgressListener { bytesDownloaded, totalBytes ->
            if (weakContext.get() == null) {
                AndroidNetworking.cancel(tag)
                isFinished = true
            } else
                callbacks?.onProgress((bytesDownloaded * 100 / totalBytes).toDouble())
        }
            .getAsObject(cls, object : ParsedRequestListener<T> {
                override fun onResponse(response: T) {
                    isFinished = true
                    callbacks?.onSuccess(response)
                }

                override fun onError(anError: ANError?) {
                    isFinished = true
                    callbacks?.onFailed(composeError(anError))

                }

            })

        while (!isFinished && weakContext.get() != null) {
            delay(500)
        }

    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getSynchronousObj(
        url: String,
        cls: Class<T>,
        tag: String,
        callbacks: GetObjectFromServerCallback<T>? = null,
        priority: Priority = Priority.LOW,
        connectTimeOut: Int = 15
    ): T? {
        val androidNet = getAndroidNet(
            url,
            tag,
            priority,
            connectTimeOut
        ).setDownloadProgressListener { bytesDownloaded, totalBytes ->
            if (weakContext.get() == null) {
                AndroidNetworking.cancel(tag)
            } else
                callbacks?.onProgress((bytesDownloaded * 100 / totalBytes).toDouble())
        }
        val result = androidNet.executeForObject(cls)
        return if (result.isSuccess) result.result as T?
        else {
            callbacks?.onFailed(composeError(result.error))
            Log.e(tag, result.error.errorBody, result.error.cause)
            null
        }

    }

    suspend fun getResponseAsString(
        url: String,
        tag: String,
        callbacks: GetObjectFromServerCallback<String?>? = null,
        priority: Priority = Priority.LOW,
        connectTimeOut: Int = 15
    ) {

        var isFinished = false
        getAndroidNet(
            url,
            tag,
            priority,
            connectTimeOut
        ).setDownloadProgressListener { bytesDownloaded, totalBytes ->
            if (weakContext.get() == null) {
                AndroidNetworking.cancel(tag)
                isFinished = true
            } else
                callbacks?.onProgress((bytesDownloaded * 100 / totalBytes).toDouble())
        }.getAsString(object : StringRequestListener {
            override fun onResponse(response: String?) {
                isFinished = true
                callbacks?.onSuccess(response)
            }

            override fun onError(anError: ANError?) {
                isFinished = true
                callbacks?.onFailed(composeError(anError))
            }

        })
        while (!isFinished && weakContext.get() != null) {
            delay(500)
        }

    }

    private fun composeError(anError: ANError?): RetError =
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