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
data class TvAboutModel(

    @SerializedName("id")
    val idTv: Int,

    @SerializedName("popularity")
    val popularity: Double,

    @SerializedName("poster_path")
    val posterPath: String?,

    @SerializedName("backdrop_path")
    val backdropPath: String?,

    @SerializedName("vote_average")
    val voteAverage: Double,

    @SerializedName("overview")
    val overview: String?,

    @SerializedName("first_air_date")
    val firstAirDate: String?,

    @SerializedName("origin_country")
    val originCountry: List<String>?,

    @SerializedName("genre_ids")
    val genres: List<Int>,

    @SerializedName("original_language")
    val originalLanguage: String?,

    @SerializedName("vote_count")
    val voteCount: Int,

    @SerializedName("name")
    val name: String?,

    @SerializedName("original_name")
    val originalName: String?,

    // This fields is originally false, but it will true if detected
    // by Repositories if this dataModel is favorite by user
    var isFavorite: Boolean = false,

    var actualGenreModel: MutableList<GenreModel>? = null
) : ResettableItem, Parcelable {

    // These fields is ignored from parcel
    // because, its functional is only in FragmentTabs
    @IgnoredOnParcel
    var nameSpan: SpannableString? = null

    @IgnoredOnParcel
    var firstAirDateSpan: SpannableString? = null


    override fun onReset() {
        if (nameSpan == null)
            nameSpan = SpannableString(name)
        if (firstAirDateSpan == null)
            firstAirDateSpan = SpannableString(firstAirDate)
        nameSpan?.clearSpans()
        firstAirDateSpan?.clearSpans()
    }
}

data class TvAboutModelResponse(

    @SerializedName("page")
    val inPage: Int,

    @SerializedName("total_results")
    val totalResults: Int,

    @SerializedName("total_pages")
    val totalPages: Int,

    @SerializedName("results")
    val contents: List<TvAboutModel>?,

    var errorCode: Int = 0,
    var errorMessage: String? = null
) : ClassResponse