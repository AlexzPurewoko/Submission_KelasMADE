package id.apwdevs.app.catalogue.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.util.LruCache
import android.widget.AdapterView
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.edit
import androidx.core.database.getStringOrNull
import id.apwdevs.app.catalogue.R
import id.apwdevs.app.catalogue.plugin.PublicContract
import id.apwdevs.app.catalogue.provider.FavoriteProvider
import id.apwdevs.app.catalogue.provider.FavoriteProvider.Companion.BASE_URI_FAVORITE
import java.io.File

class StackRemoteViewsFactory(
    private val context: Context,
    private val appWidgetId: Int
) : RemoteViewsService.RemoteViewsFactory {

    private var mCursor: Cursor? = null
    private val mPosterWidth = context.resources.getDimension(R.dimen.item_poster_width)
    private val mPosterHeight = context.resources.getDimension(R.dimen.item_poster_height)
    private val mBitmapCache: LruCache<Int, Bitmap> = LruCache((mPosterHeight * mPosterWidth * 6).toInt())
    private val mData: MutableList<ItemWidget> = mutableListOf()
    override fun onCreate() {
    }

    override fun getLoadingView(): RemoteViews = RemoteViews(context.packageName, R.layout.widget_loading)

    override fun getItemId(position: Int): Long = 0

    override fun onDataSetChanged() {
        val idType = context.getSharedPreferences(PublicContract.WIDGET_SHARED_PREFERENCES, Context.MODE_PRIVATE)
            .getInt("widget_conf_${appWidgetId}_type", -1)
        if (idType == -1) return
        val id = Binder.clearCallingIdentity()
        mCursor?.close()
        mCursor = context.contentResolver.query(BASE_URI_FAVORITE.apply {
            appendPath(FavoriteProvider.FAV_TYPE)
            appendPath(idType.toString())
        }.build(), null, null, null, null)
        val newList = mutableListOf<ItemWidget>()
        mCursor?.apply {
            moveToFirst()
            while (!isAfterLast) {
                val contentID = getInt(getColumnIndex("id"))
                val posterPath = getStringOrNull(getColumnIndex("posterPath"))
                val title = getStringOrNull(getColumnIndex("title"))
                newList.add(
                    ItemWidget(
                        posterPath, title, contentID
                    )
                )
                moveToNext()
            }
            moveToFirst()
        }
        compareAndPerformOperation(newList)
        Binder.restoreCallingIdentity(id)
    }

    private fun compareAndPerformOperation(newList: MutableList<ItemWidget>) {
        if (mData.isEmpty()) {
            mData.addAll(newList)
            return
        }
        var idx = 0
        while (true) {
            if (idx >= mData.size)
                break
            val data = mData[idx]
            if (newList.contains(data)) {
                newList.remove(data)
                idx++
            } else {
                mBitmapCache.remove(data.id)
                mData.remove(data)
            }
        }

        mData.addAll(newList)
    }

    override fun hasStableIds(): Boolean = false

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.favorite_stackwidget_item)
        mCursor?.apply {
            if (position == AdapterView.INVALID_POSITION || !moveToPosition(position)) {
                return@apply
            }
            views.apply {
                val id = getInt(getColumnIndex("id"))
                setImageBitmap(position, this)
                setTextViewText(R.id.widget_fav_item_title, getString(getColumnIndex("title")))
                setOnClickFillInIntent(R.id.favorite_widget_btn, Intent().apply {
                    `package` = context.packageName
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(FavoriteWidget.ACTION_TYPE, FavoriteWidget.STACK_LAUNCH_DETAIL_ITEM)
                    putExtra(FavoriteWidget.STACK_ITEM_ID, id)
                })
            }
        }
        return views
    }

    private fun setImageBitmap(position: Int, remoteViews: RemoteViews) {
        val data = mData[position]
        data.posterPath?.let {
            var bmp = mBitmapCache.get(data.id)
            if (bmp == null) {
                val file = File(File(context.filesDir, PublicContract.FAVORITE_POSTER_PATH).apply {
                    mkdirs()
                }, it)
                if (file.exists()) {
                    bmp = BitmapFactory.decodeFile(file.absolutePath)
                    mBitmapCache.put(data.id, bmp)
                }
            }
            remoteViews.setImageViewBitmap(R.id.stackview_widget_item, bmp)
        }
    }

    override fun getCount(): Int = mCursor?.count ?: 0

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {
        context.getSharedPreferences(PublicContract.WIDGET_SHARED_PREFERENCES, Context.MODE_PRIVATE)
            .edit(commit = true) {
                remove("widget_conf_${appWidgetId}_type")
        }
        mCursor?.close()
        mBitmapCache.evictAll()
    }

    private data class ItemWidget(
        val posterPath: String?,
        val title: String?,
        val id: Int
    )
}