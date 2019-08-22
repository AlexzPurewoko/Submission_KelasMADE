package id.apwdevs.app.catalogue.widget

import android.content.Context
import android.content.Intent
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

    private val mPosterWidth = context.resources.getDimension(R.dimen.item_poster_width)
    private val mPosterHeight = context.resources.getDimension(R.dimen.item_poster_height)
    private val mBitmapCache: LruCache<Int, Bitmap> =
        LruCache((mPosterHeight * mPosterWidth * 6).toInt())
    private var mData: MutableList<ItemWidget>? = null
    override fun onCreate() {
    }

    override fun getLoadingView(): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_loading)

    override fun getItemId(position: Int): Long = 0

    override fun onDataSetChanged() {
        val idType = context.getSharedPreferences(
            PublicContract.WIDGET_SHARED_PREFERENCES,
            Context.MODE_PRIVATE
        )
            .getInt("widget_conf_${appWidgetId}_type", -1)
        if (idType == -1) return
        val id = Binder.clearCallingIdentity()
        val newList = mutableListOf<ItemWidget>()
        val mCursor = context.contentResolver.query(BASE_URI_FAVORITE.apply {
            appendPath(FavoriteProvider.FAV_TYPE)
            appendPath(idType.toString())
        }.build(), null, null, null, null)
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
            compareAndPerformOperation(newList)
        }
        mCursor?.close()
        Binder.restoreCallingIdentity(id)
    }

    private fun compareAndPerformOperation(newList: MutableList<ItemWidget>) {
        if (mData.isNullOrEmpty()) {
            mData = mutableListOf()
            mData?.addAll(newList)
            return
        }
        mData?.let {
            var idx = 0
            while (true) {
                if (idx >= it.size)
                    break
                val data = it[idx]
                if (newList.contains(data)) {
                    newList.remove(data)
                    idx++
                } else {
                    mBitmapCache.remove(data.id)
                    it.remove(data)
                }
            }
            it.addAll(newList)
        }
    }

    override fun hasStableIds(): Boolean = false

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.favorite_stackwidget_item)
        if (!mData.isNullOrEmpty())
            mData?.get(position)?.apply {
                if (position == AdapterView.INVALID_POSITION) {
                    return@apply
                }
                views.apply {
                    setImageBitmap(position, this)
                    setTextViewText(R.id.widget_fav_item_title, title)
                    setOnClickFillInIntent(R.id.stackview_widget_item, Intent().apply {
                        putExtra(FavoriteWidget.ACTION_TYPE, FavoriteWidget.SHORT_EXPLANATION)
                        putExtra(FavoriteWidget.STACK_ITEM_TITLE, title)
                        putExtra(FavoriteWidget.STACK_ITEM_ID, id)
                        putExtra(FavoriteWidget.STACK_ITEM_POSITION, position)
                    })
                    setOnClickFillInIntent(R.id.favorite_widget_btn, Intent().apply {
                        putExtra(
                            FavoriteWidget.ACTION_TYPE,
                            FavoriteWidget.STACK_LAUNCH_DETAIL_ITEM
                        )
                        putExtra(FavoriteWidget.STACK_ITEM_ID, id)
                    })
                }
            }
        return views
    }

    private fun setImageBitmap(position: Int, remoteViews: RemoteViews) {
        mData?.get(position)?.apply {
            posterPath?.let {
                var bmp = mBitmapCache.get(id)
                if (bmp == null) {
                    val file =
                        File(File(context.filesDir, PublicContract.FAVORITE_POSTER_PATH).apply {
                            mkdirs()
                        }, it)
                    if (file.exists()) {
                        bmp = BitmapFactory.decodeFile(file.absolutePath)
                        mBitmapCache.put(id, bmp)
                    }
                }
                remoteViews.setImageViewBitmap(R.id.stackview_widget_item, bmp)
            }
        }

    }

    override fun getCount(): Int = mData?.size ?: 0

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {
        context.getSharedPreferences(PublicContract.WIDGET_SHARED_PREFERENCES, Context.MODE_PRIVATE)
            .edit(commit = true) {
                remove("widget_conf_${appWidgetId}_type")
            }
        mBitmapCache.evictAll()
        mData?.clear()
        mData = null
    }

    private data class ItemWidget(
        val posterPath: String?,
        val title: String?,
        val id: Int
    )
}