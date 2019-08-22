package id.apwdevs.app.catalogue.plugin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.WorkerThread
import androidx.core.graphics.drawable.toBitmap
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.database.FavoriteDatabase
import id.apwdevs.app.catalogue.entity.FavoriteEntity
import id.apwdevs.app.catalogue.model.GenreModel
import id.apwdevs.app.catalogue.model.ResettableItem
import id.apwdevs.app.catalogue.model.onUserMain.MainDataItemModel
import id.apwdevs.app.catalogue.provider.FavoriteProvider
import java.io.File
import java.io.FileOutputStream

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

@WorkerThread
fun configureFavorite(
    context: Context,
    model: ResettableItem?,
    posterDrawable: Drawable?
): Boolean =
    model?.let {
        val db = FavoriteDatabase.getInstance(context)
        val favDao = db.favoriteDao()
        var posterPath: String? = null
        val id: Int = when (it) {
            is MainDataItemModel -> {
                posterPath = it.posterPath
                it.id
            }
            is FavoriteEntity -> {
                posterPath = it.posterPath
                it.id
            }
            else -> -1
        }
        if (id == -1) return@let false
        val currentIsFavorite: Boolean = when (it) {
            is MainDataItemModel -> it.isFavorite
            is FavoriteEntity -> true
            else -> return@let false
        }

        when (currentIsFavorite) {
            false ->
                when (it) {
                    is MainDataItemModel -> {
                        FavoriteEntity(
                            it.id,
                            "${it.title}",
                            it.contentTypes,
                            "${it.releaseDate}",
                            it.overview,
                            converToStr(it.actualGenreModel),
                            it.posterPath,
                            it.backdropPath,
                            it.voteCount,
                            it.voteAverage
                        )
                    }
                    else -> null
                }?.let { itsModel ->
                    favDao.addToFavorites(listOf(itsModel))
                }
            true -> favDao.removeAt(id)
        }
        val isFav = favDao.isAnyColumnIn(id)
        if (it is MainDataItemModel) it.isFavorite = isFav
        if (isFav) {
            posterDrawable?.let {
                posterPath?.let { posterPath ->
                    val bmp = it.toBitmap()
                    val file =
                        File(File(context.filesDir, PublicContract.FAVORITE_POSTER_PATH).apply {
                            mkdirs()
                        }, posterPath)
                    val fos = FileOutputStream(file)
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                    fos.flush()
                    fos.close()
                }
            }
            context.contentResolver.notifyChange(FavoriteProvider.BASE_URI_FAVORITE.build(), null)
        } else if (!isFav && currentIsFavorite) {
            File(File(context.cacheDir, PublicContract.FAVORITE_POSTER_PATH).apply {
                mkdirs()
            }, posterPath).delete()
            context.contentResolver.notifyChange(FavoriteProvider.BASE_URI_FAVORITE.build(), null)
        }
        isFav
    } ?: false


private fun converToStr(gModel: List<GenreModel>?): String? =
    gModel?.let {
        if (it.isEmpty()) return@let null
        val sbuf = StringBuffer()
        it.forEach { genre ->
            sbuf.append(genre.genreName)
            sbuf.append(",")
        }
        if (sbuf[sbuf.length - 1] == ',')
            sbuf.deleteCharAt(sbuf.length - 1)
        sbuf.toString()
    }





