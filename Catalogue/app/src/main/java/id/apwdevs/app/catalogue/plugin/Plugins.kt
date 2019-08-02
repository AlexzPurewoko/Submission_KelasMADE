package id.apwdevs.app.catalogue.plugin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.WorkerThread
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.BitmapRequestListener
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.database.FavoriteDatabase
import id.apwdevs.app.catalogue.entity.FavoriteEntity
import id.apwdevs.app.catalogue.model.GenreModel
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onUserMain.MovieAboutModel
import id.apwdevs.app.catalogue.model.onUserMain.TvAboutModel
import id.apwdevs.app.catalogue.plugin.api.GetImageFiles

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

@Deprecated("This method will be removed, use GetObjectFromServer.getBitmapNoProgress() instead")
fun getBitmap(
    size: Point,
    posterPath: String,
    scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_XY,
    response: (response: Bitmap?) -> Unit
) {
    AndroidNetworking.get(GetImageFiles.getImg(size.x, posterPath))
        .setPriority(Priority.LOW)
        .setBitmapMaxHeight(size.y)
        .setBitmapMaxWidth(size.x)
        .setImageScaleType(scaleType)
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

@WorkerThread
fun configureFavorite(context: Context, model: ResettableItem?): Boolean =
    model?.let {
        val db = FavoriteDatabase.getInstance(context)
        val favDao = db.favoriteDao()
        val id: Int = when (it) {
            is MovieAboutModel -> it.id
            is TvAboutModel -> it.idTv
            is FavoriteEntity -> it.id
            else -> -1
        }
        if (id == -1) return@let false
        val currentIsFavorite: Boolean
        // if this scope is Favorite Fragment, then it will be removed from lists
        /*if (it is FavoriteEntity) {
            favDao.removeAt(id)
            // notify into implemented fragment or activity
            mContext.runOnUiThread {
                reqRefreshRootDataSets()
            }
            return@launch
        }*/

        when (it) {
            is MovieAboutModel -> currentIsFavorite = it.isFavorite
            is TvAboutModel -> currentIsFavorite = it.isFavorite
            is FavoriteEntity -> currentIsFavorite = true
            else -> return@let false
        }

        when (currentIsFavorite) {
            false ->
                when (it) {
                    is MovieAboutModel -> {
                        FavoriteEntity(
                            it.id,
                            "${it.title}",
                            PublicContract.ContentDisplayType.MOVIE.type,
                            "${it.releaseDate}",
                            it.overview,
                            converToStr(it.actualGenreModel),
                            it.posterPath,
                            it.backdropPath,
                            it.voteCount,
                            it.voteAverage
                        )
                    }
                    is TvAboutModel -> {
                        FavoriteEntity(
                            it.idTv,
                            "${it.name}",
                            PublicContract.ContentDisplayType.TV_SHOWS.type,
                            "${it.firstAirDate}",
                            it.overview,
                            converToStr(it.actualGenreModel),
                            it.posterPath,
                            it.backdropPath,
                            it.voteCount,
                            it.voteAverage
                        )
                    }
                    else -> null
                }?.let {
                    favDao.addToFavorites(listOf(it))
                }
            true -> favDao.removeAt(id)
        }
        val isFav = favDao.isAnyColumnIn(id)
        when (it) {
            is MovieAboutModel -> it.isFavorite = isFav
            is TvAboutModel -> it.isFavorite = isFav
        }
        isFav
    } ?: false

private fun converToStr(gModel: List<GenreModel>?): String {
    val sbuf = StringBuffer()
    gModel?.forEach {
        sbuf.append(it.genreName)
        sbuf.append(",")
    }
    if (sbuf[sbuf.length - 1].equals(','))
        sbuf.deleteCharAt(sbuf.length - 1)
    return sbuf.toString()
}