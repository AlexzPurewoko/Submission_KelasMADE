package id.apwdevs.app.catalogue.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Binder
import android.widget.AdapterView
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.database.getStringOrNull
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.entity.FavoriteEntity
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.plugin.PublicContract.ContentDisplayType
import id.apwdevs.app.catalogue.plugin.api.GetObjectFromServer
import id.apwdevs.app.catalogue.provider.FavoriteProvider
import id.apwdevs.app.catalogue.provider.FavoriteProvider.Companion.BASE_URI_FAVORITE
import id.apwdevs.app.catalogue.receiver.LaunchReceiver.Companion.EXTRA_ID

class StackRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var mCursor: Cursor? = null
    private val mListBitmapRef: HashMap<Int, Bitmap?> = hashMapOf()

    private val requestedWidth: Int = context.resources.getDimension(R.dimen.item_poster_width).toInt()
    private val requestedHeight: Int = context.resources.getDimension(R.dimen.item_poster_height).toInt()
    override fun onCreate() {
        /*GlobalScope.launch (Dispatchers.IO){
            mListData.addAll(getFavorite(context, PublicContract.ContentDisplayType.MOVIE))
            Log.d("LoadAllDb", "list -> $mListData")
        }*/
    }

    override fun getLoadingView(): RemoteViews = RemoteViews(context.packageName, R.layout.widget_loading)

    override fun getItemId(position: Int): Long = 0

    override fun onDataSetChanged() {
        val id = Binder.clearCallingIdentity()
        mCursor?.close()
        mListBitmapRef.clear()
        mCursor = context.contentResolver.query(BASE_URI_FAVORITE.apply {
            appendPath(FavoriteProvider.FAV_TYPE)
            appendPath(ContentDisplayType.MOVIE.type.toString())
        }.build(), null, null, null, null)
        mCursor?.apply {
            moveToFirst()
            val size = Point(requestedWidth, requestedHeight)
            while (!isAfterLast) {
                getStringOrNull(getColumnIndex("posterPath"))?.let {
                    var bmp: Bitmap? = null
                    var isFinished = false
                    val mId = getInt(getColumnIndex("id"))
                    GetObjectFromServer.getInstance(context)
                        .getBitmapNoProgress(size, it, true, forceLoadFromCache = true) { bitmap ->
                            bmp = bitmap
                            isFinished = true
                        }
                    while (!isFinished) Thread.sleep(100)
                    mListBitmapRef.put(mId, bmp)
                }
                moveToNext()
            }
            moveToFirst()
        }
        Binder.restoreCallingIdentity(id)
    }

    override fun hasStableIds(): Boolean = false

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.favorite_stackwidget_item)
        mCursor?.apply {
            if (position == AdapterView.INVALID_POSITION || !moveToPosition(position)) {
                return@apply
            }
            views.apply {
                mListBitmapRef[getInt(getColumnIndex("id"))]?.let {
                    setImageViewBitmap(R.id.stackview_widget_item, it)
                }
                val id = getInt(getColumnIndex("id"))
                setTextViewText(R.id.widget_fav_item_title, getString(getColumnIndex("title")))
                /*setOnClickFillInIntent(R.id.favorite_widget_btn, Intent().apply {
                    `package` = context.packageName
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(FavoriteWidget.UPDATE_TYPE, FavoriteWidget.STACK_REMOVE_ITEM)
                    putExtra(FavoriteWidget.STACK_ITEM_ID, id)
                })*/
                setOnClickFillInIntent(R.id.favorite_widget_btn, Intent().also {
                    it.`package` = context.packageName
                    it.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    it.putExtra(EXTRA_ID, id)
                })
            }
        }
        return views
    }

    override fun getCount(): Int = mCursor?.count ?: 0

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {
        mCursor?.close()
        mListBitmapRef.clear()
    }


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