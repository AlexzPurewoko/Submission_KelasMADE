package id.apwdevs.app.catalogue.model.onUserMain

import android.os.Bundle
import android.os.Parcelable
import android.text.SpannableString
import androidx.core.text.clearSpans
import com.google.gson.annotations.SerializedName
import id.apwdevs.app.catalogue.model.ClassResponse
import id.apwdevs.app.catalogue.model.GenreModel
import id.apwdevs.app.catalogue.model.ResettableItem
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MainDataItemModel(
    @SerializedName("id")
    val id: Int,

    @SerializedName(value = "release_date", alternate = ["first_air_date"])
    val releaseDate: String?,

    @SerializedName("poster_path")
    val posterPath: String?,

    @SerializedName("overview")
    val overview: String?,

    @SerializedName("genre_ids")
    val genres: List<Int>,

    @SerializedName(value = "original_title", alternate = ["original_name"])
    val originalTitle: String?,

    @SerializedName("original_language")
    val originalLanguage: String?,

    @SerializedName(value = "title", alternate = ["name"])
    val title: String?,

    @SerializedName("backdrop_path")
    val backdropPath: String?,

    @SerializedName("vote_count")
    val voteCount: Int,

    @SerializedName("vote_average")
    val voteAverage: Double,

    // This fields is originally false, but it will true if detected
    // by Repositories if this dataModel is favorite by user
    var isFavorite: Boolean = false,

    var actualGenreModel: MutableList<GenreModel>? = null
) : Parcelable, ResettableItem {
    // These fields is ignored from parcel
    @IgnoredOnParcel
    var titleSpan: SpannableString? = null

    @IgnoredOnParcel
    var releaseDateSpan: SpannableString? = null

    fun fromDataIntoBundle(): Bundle {
        return Bundle().apply {
            putInt(MODEL_ID, id)
            putString(MODEL_RELDATE, releaseDate)
            putString(MODEL_POSTERPATH, posterPath)
            putString(MODEL_OVERVIEW, overview)
            putIntegerArrayList(MODEL_GENRES, ArrayList(genres))
            putString(MODEL_ORI_TITLE, originalTitle)
            putString(MODEL_ORI_LANG, originalLanguage)
            putString(MODEL_TILE, title)
            putString(MODEL_BACKDROP_PATH, backdropPath)
            putInt(MODEL_VOTE_COUNT, voteCount)
            putDouble(MODEL_VOTE_AVERAGE, voteAverage)
            putBoolean(MODEL_IS_FAV, isFavorite)
        }
    }

    companion object {
        const val MODEL_ID = "MODEL_ID"
        const val MODEL_RELDATE = "MODEL_RELDATE"
        const val MODEL_POSTERPATH = "MODEL_POSTERPATH"
        const val MODEL_OVERVIEW = "MODEL_OVERVIEW"
        const val MODEL_GENRES = "MODEL_GENRES"
        const val MODEL_ORI_TITLE = "MODEL_ORI_TITLE"
        const val MODEL_ORI_LANG = "MODEL_ORI_LANG"
        const val MODEL_TILE = "MODEL_TITLE"
        const val MODEL_BACKDROP_PATH = "MODEL_BACKDROP_PATH"
        const val MODEL_VOTE_COUNT = "MODEL_VOTE_COUNT"
        const val MODEL_VOTE_AVERAGE = "MODEL_VOTE_AVERAGE"
        const val MODEL_IS_FAV = "MODEL_IS_FAV"
        fun fromBundleIntoModel(bundle: Bundle): MainDataItemModel {
            bundle.apply {
                return MainDataItemModel(
                    getInt(MODEL_ID, 0),
                    getString(MODEL_RELDATE, ""),
                    getString(MODEL_POSTERPATH, ""),
                    getString(MODEL_OVERVIEW, ""),
                    getIntegerArrayList(MODEL_GENRES) ?: listOf(),
                    getString(MODEL_ORI_TITLE, ""),
                    getString(MODEL_ORI_LANG, ""),
                    getString(MODEL_TILE, ""),
                    getString(MODEL_BACKDROP_PATH, ""),
                    getInt(MODEL_VOTE_COUNT, 0),
                    getDouble(MODEL_VOTE_AVERAGE, 0.0),
                    getBoolean(MODEL_IS_FAV, false)
                )
            }
        }
    }

    override fun onReset() {
        if (titleSpan == null)
            titleSpan = SpannableString(title)
        if (releaseDateSpan == null)
            releaseDateSpan = SpannableString(releaseDate)

        titleSpan?.clearSpans()
        releaseDateSpan?.clearSpans()

    }
}

@Parcelize
data class MainDataItemResponse(

    @SerializedName("page")
    val inPage: Int,

    @SerializedName("total_results")
    val totalResults: Int,

    @SerializedName("total_pages")
    val totalPages: Int,

    @SerializedName("results")
    val contents: List<MainDataItemModel>?,

    var errorCode: Int = 0,
    var errorMessage: String? = null
) : ClassResponse, Parcelable {

    fun fromDataIntoBundle(): Bundle {
        return Bundle().apply {
            putInt(RESPONSE_IN_PAGE, inPage)
            putInt(RESPONSE_TOTAL_RESULT, totalResults)
            putInt(RESPONSE_TOTAL_PAGES, totalPages)
            putString(RESPONSE_ERR_MSG, errorMessage)
            putInt(RESPONSE_ERR_CODE, errorCode)

            contents?.let {
                for ((idx, model) in it.withIndex()) {
                    putBundle("$RESPONSE_DATA_BUNDLE$idx", model.fromDataIntoBundle())
                    return@let
                }
                putBundle(RESPONSE_DATA_BUNDLE, Bundle.EMPTY)
            }
        }
    }

    companion object {
        const val RESPONSE_IN_PAGE = "RESPONSE_IN_PAGE"
        const val RESPONSE_TOTAL_RESULT = "RESPONSE_TOTAL_RESULT"
        const val RESPONSE_TOTAL_PAGES = "RESPONSE_TOTAL_PAGES"
        const val RESPONSE_ERR_MSG = "RESPONSE_ERR_MSG"
        const val RESPONSE_ERR_CODE = "RESPONSE_ERR_CODE"
        const val RESPONSE_DATA_BUNDLE = "mainDataItemModelAtIdx"
        fun fromBundleIntoModel(bundle: Bundle): MainDataItemResponse {
            bundle.apply {
                val a = getInt(RESPONSE_IN_PAGE, 0)
                val b = getInt(RESPONSE_TOTAL_RESULT, 0)
                val c = getInt(RESPONSE_TOTAL_PAGES, 0)
                val d = getString(RESPONSE_ERR_MSG)
                val e = getInt(RESPONSE_ERR_CODE, 0)
                val listModel: MutableList<MainDataItemModel>?
                if (getBundle(RESPONSE_DATA_BUNDLE) == Bundle.EMPTY) {
                    listModel = null
                } else {
                    listModel = mutableListOf()
                    var idx = 0
                    while (true) {
                        val key = "$RESPONSE_DATA_BUNDLE$idx"
                        if (containsKey(key)) {
                            getBundle(key)?.let { listModel.add(MainDataItemModel.fromBundleIntoModel(it)) }
                            idx++
                        } else break
                    }
                }

                return MainDataItemResponse(a, b, c, listModel, e, d)

            }


        }
    }
}