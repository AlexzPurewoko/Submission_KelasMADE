package id.apwdevs.app.catalogue.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import id.apwdevs.app.catalogue.database.FavoriteDatabase

class FavoriteProvider : ContentProvider() {
    companion object {
        const val FAV_MATCH = 0xa
        const val FAVORITE_ID = 0x4f
        const val GENRE_MATCH = 0X5f
        const val FAVORITE_TYPE = 0x44f

        @JvmStatic
        val FAVORITE_TABLE = "favorite"
        @JvmStatic
        val GENRE_TABLE = "genres"
        @JvmStatic
        val AUTHORITY = "id.apwdevs.app.catalogue"
        @JvmStatic
        val SCHEME = "content"
        @JvmStatic
        val FAV_TYPE = "type"

        @JvmStatic
        val BASE_URI_FAVORITE: Uri.Builder
            get() {
                return Uri.Builder().apply {
                    scheme(SCHEME)
                    authority(AUTHORITY)
                    appendPath(FAVORITE_TABLE)
                }
            }

        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, FAVORITE_TABLE, FAV_MATCH)
            addURI(AUTHORITY, "$FAVORITE_TABLE/$FAV_TYPE/#", FAVORITE_TYPE)
            addURI(AUTHORITY, "$FAVORITE_TABLE/#", FAVORITE_ID)
            addURI(AUTHORITY, GENRE_TABLE, GENRE_MATCH)
        }
    }

    private var favDb: FavoriteDatabase? = null


    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? =
        favDb?.let { fDb ->
            val favDao = fDb.favoriteDao()
            val genreDao = fDb.genreDao()
            when (sUriMatcher.match(uri)) {
                FAV_MATCH -> favDao.getAllByCursor()
                FAVORITE_ID ->
                    uri.lastPathSegment?.let {
                        favDao.getItemAtByCursor(it.toInt())
                    }
                FAVORITE_TYPE ->
                    uri.lastPathSegment?.let {
                        favDao.getAsTypeByCursor(it.toInt())
                    }
                GENRE_MATCH ->
                    genreDao.getAllByCursor()
                else -> null
            }
        }

    override fun onCreate(): Boolean {
        context?.let {
            favDb = FavoriteDatabase.getInstance(it)
            return true
        }
        return false
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = -1

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val retValue = favDb?.let {
            var deleted: Int = -1
            if (sUriMatcher.match(uri) == FAVORITE_ID) {
                uri.lastPathSegment?.let { lastSegment ->
                    val fDao = it.favoriteDao()
                    val idx = lastSegment.toInt()
                    fDao.removeAt(idx)
                    if (fDao.getItemAt(idx) == null) {
                        deleted = idx
                    }
                }
            }
            deleted
        } ?: -1

        if (retValue != -1) {
            context?.contentResolver?.notifyChange(uri, null)
        }
        return retValue
    }

    override fun getType(uri: Uri): String? = null

}