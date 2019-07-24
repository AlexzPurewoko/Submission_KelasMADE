package id.apwdevs.app.catalogue.plugin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.BitmapRequestListener
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.plugin.api.GetImageFiles


fun jsonCheckAndGet(get: Any): Any? =
    if (get.toString() == "null") null
    else get

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun calculateMaxColumn(mContext: Context, windowSize: Point): Int {
    val resources = mContext.resources
    val vPagerMarginStart = resources.getDimension(R.dimen.viewpager_user_marginstart)
    val vPagerMarginEnd = resources.getDimension(R.dimen.viewpager_user_marginend)
    val cardViewPadding = resources.getDimension(R.dimen.cardview_grid_content_padding)
    val imageViewWidth = resources.getDimension(R.dimen.item_poster_width)

    var maxColumn = 1
    while (true) {
        val contentMeasuredWidth = imageViewWidth + 2 * cardViewPadding
        val spaceFreeForContent =
            windowSize.x.toFloat() - (vPagerMarginStart + vPagerMarginEnd) - maxColumn * contentMeasuredWidth
        if (spaceFreeForContent > contentMeasuredWidth) {
            maxColumn++
        } else {
            break
        }
    }
    return maxColumn
}

fun getCurrency(prefix: String?, value: String?): String {
    if (value.isNullOrBlank()) return "-"
    val bf = StringBuffer()
    var point = 0
    var idx = value.length - 1
    while (idx > -1) {
        if (point == 3) {
            bf.append(".")
            point = 0
        }
        bf.append(value[idx--])
        point++
    }
    return "$prefix ${bf.reverse()}"
}

fun getReadableTime(inMinute: Int?): String {
    if (inMinute == null) return "-"
    val hour: Int = if (inMinute < 60) 0 else inMinute / 60
    val minutes: Int
    var temp: Int = inMinute

    // get Hours
    while (true) {
        if (temp < 60) {
            minutes = temp
            break
        }
        temp -= 60
    }
    return "${hour}h ${minutes}m"
}

fun getBitmap(size: Point, posterPath: String, response: (response: Bitmap?) -> Unit) {
    AndroidNetworking.get(GetImageFiles.getImg(size.x, posterPath))
        .setPriority(Priority.LOW)
        .setBitmapMaxHeight(size.y)
        .setBitmapMaxWidth(size.x)
        .setImageScaleType(ImageView.ScaleType.FIT_XY)
        .build()
        .getAsBitmap(object : BitmapRequestListener {
            override fun onResponse(response: Bitmap?) {
                response(response)
            }

            override fun onError(anError: ANError?) {
                Log.e("ErrorDisplayBitmap", anError?.errorBody, anError)
            }

        })
}