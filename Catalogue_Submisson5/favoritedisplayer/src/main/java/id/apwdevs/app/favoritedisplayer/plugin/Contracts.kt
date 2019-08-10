package id.apwdevs.app.favoritedisplayer.plugin

import android.content.Context
import android.net.Uri
import androidx.core.database.getStringOrNull
import id.apwdevs.app.favoritedisplayer.model.FavoriteEntity
import id.apwdevs.app.favoritedisplayer.repository.MainListRepository

object Contracts {
    @JvmStatic
    val FAVORITE_TABLE = "favorite"
    @JvmStatic
    val AUTHORITY = "id.apwdevs.app.catalogue"
    @JvmStatic
    val SCHEME = "content"

    @JvmStatic
    val BASE_URI_FAVORITE: Uri.Builder
        get() {
            return Uri.Builder().apply {
                scheme(SCHEME)
                authority(AUTHORITY)
                appendPath(FAVORITE_TABLE)
            }
        }

    fun removeFromFavorite(context: Context, id: Int): Int =
        context.contentResolver.delete(BASE_URI_FAVORITE.appendPath(id.toString()).build(), null, null)

    fun getFavorite(context: Context, displayType: MainListRepository.ContentDisplayType): List<FavoriteEntity> {
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
                val currTypes = MainListRepository.ContentDisplayType.findId(contentType)
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
