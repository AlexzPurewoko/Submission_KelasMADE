package id.apwdevs.app.catalogue.widget

import android.content.Context
import android.graphics.Point
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.database.getStringOrNull
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.entity.FavoriteEntity
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.plugin.api.GetImageFiles
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.provider.FavoriteProvider.Companion.BASE_URI_FAVORITE

class StackRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    var mListData: MutableList<FavoriteEntity> = mutableListOf()
    override fun onCreate() {
        mListData.addAll(getFavorite(context, PublicContract.ContentDisplayType.MOVIE))
    }

    override fun getLoadingView(): RemoteViews = RemoteViews(context.packageName, R.layout.favorite_stackwidget_item)

    override fun getItemId(position: Int): Long = 0

    override fun onDataSetChanged() {

    }

    override fun hasStableIds(): Boolean = false

    override fun getViewAt(position: Int): RemoteViews =
        RemoteViews(context.packageName, R.layout.favorite_stackwidget_item).apply {
            val item = mListData[position]
            GetObjectFromServer.getInstance(context).apply {
                val size = Point(300, 300)
                item.posterPath?.let {
                    getBitmapNoProgress(size, GetImageFiles.getImg(size.x, it), false) { bitmap ->
                        setImageViewBitmap(R.id.stackview_widget_item, bitmap)
                    }
                }
                setTextViewText(R.id.widget_fav_item_title, item.title)
            }
        }

    override fun getCount(): Int = mListData.size

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {

    }

    fun removeFromFavorite(context: Context, id: Int): Int =
        context.contentResolver.delete(BASE_URI_FAVORITE.appendPath(id.toString()).build(), null, null)


    fun getFavorite(context: Context, displayType: PublicContract.ContentDisplayType): List<FavoriteEntity> {
        val uri = BASE_URI_FAVORITE.build()
        val mCursor = context.contentResolver.query(uri, null, null, null, null)
        val mListFavorites: MutableList<FavoriteEntity> = mutableListOf()
        mCursor?.apply {
            moveToFirst()
            while (!isAfterLast) {
                val id = getInt(getColumnIndex("id"))
                val title = getString(getColumnIndex("title"))
                val contentType = getInt(getColumnIndex("contentType"))
                val releaseDate = getString(getColumnIndex("releaseDate"))
                val overview = getStringOrNull(getColumnIndex("overview"))
                val genreIds = getStringOrNull(getColumnIndex("genreIds"))
                val posterPath = getStringOrNull(getColumnIndex("posterPath"))
                val backdropPath = getStringOrNull(getColumnIndex("backdropPath"))
                val voteCount = getInt(getColumnIndex("voteCount"))
                val voteAverage = getDouble(getColumnIndex("voteAverage"))
                val currTypes = PublicContract.ContentDisplayType.findId(contentType)
                if (currTypes == displayType) {
                    mListFavorites.add(
                        FavoriteEntity(
                            id,
                            title,
                            contentType,
                            releaseDate,
                            overview,
                            genreIds,
                            posterPath,
                            backdropPath,
                            voteCount,
                            voteAverage
                        )
                    )
                }
                moveToNext()
            }

        }
        mCursor?.close()
        return mListFavorites
    }
}