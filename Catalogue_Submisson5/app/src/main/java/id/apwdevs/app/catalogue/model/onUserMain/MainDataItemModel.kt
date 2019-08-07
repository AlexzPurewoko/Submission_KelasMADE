package id.apwdevs.app.catalogue.model.onUserMain

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


    override fun onReset() {
        if (titleSpan == null)
            titleSpan = SpannableString(title)
        if (releaseDateSpan == null)
            releaseDateSpan = SpannableString(releaseDate)

        titleSpan?.clearSpans()
        releaseDateSpan?.clearSpans()

    }
}

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
) : ClassResponse