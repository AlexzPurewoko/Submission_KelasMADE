package id.apwdevs.app.catalogue.plugin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

object PublicContract {

    const val SHARED_PREF_GLOBAL_NAME = "id.apwdevs.app.catalogue_preferences"
    const val TV_OTA: String = "on_the_air"
    const val TV_AIRING_TODAY: String = "airing_today"
    const val GENRE: String = "genre"
    const val NOW_PLAYING: String = "now_playing"
    const val POPULAR: String = "popular"
    const val UPCOMING: String = "upcoming"
    const val TOP_RATED: String = "top_rated"
    const val DISCOVER_PATH: String = "discover"

    const val TV_DIR_PATH: String = "tv"
    const val CREDITS_QNAME: String = "credits"
    const val REVIEWS_QNAME: String = "reviews"
    const val API_KEY = "dbf51ebeff5d2813971bdfe87e3fb013"
    const val URL_API = "https://api.themoviedb.org/3"
    const val MOVIE_DIR_PATH = "movie"
    const val EXTERNAL_ID_QNAME = "external_ids"
    const val API_KEY_QNAME = "api_key"


    object DatabaseContract {
        const val DATABASE_FAVORITE_NAME = "catalogue_data"
        const val TABLE_GENRES = "all_genre"
        const val TABLE_FAVORITES = "my_favorites"
    }

    object RecyclerMode {
        const val MODE_GRID = 0x6ffa
        const val MODE_LIST = 0x5faa
        const val MODE_STAGERRED_LIST = 0xaf45
    }


    @Parcelize
    enum class ContentDisplayType(val type: Int) : Parcelable {
        MOVIE(0x2a),
        TV_SHOWS(0x1f),
        FAVORITES(0x1d);

        companion object {
            fun findId(idType: Int): ContentDisplayType? {
                values().iterator().forEach {
                    if (it.type == idType)
                        return it
                }
                return null
            }
        }
    }

}