package id.apwdevs.app.favoritedisplayer.model.onDetail

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import id.apwdevs.app.favoritedisplayer.model.ResettableItem
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OtherTVAboutModel(

    @SerializedName("created_by")
    val createdBy: MutableList<ModelTvCreatedBy>,

    @SerializedName("homepage")
    val homepage: String,

    @SerializedName("in_production")
    val inProduction: Boolean,

    @SerializedName("last_air_date")
    val lastAirDate: String,

    @SerializedName("number_of_episodes")
    val numberOfEpisodes: Int,

    @SerializedName("number_of_seasons")
    val numberOfSeasons: Int,

    @SerializedName("origin_country")
    val originCountry: List<String>,

    @SerializedName("status")
    val status: String,

    @SerializedName("type")
    val type: String
) : Parcelable, ResettableItem {
    override fun onReset() {

    }

}

@Deprecated("Not In Usages, will be removed")
@Parcelize
data class ProductionTVSeasons(
    val airDate: String,
    val episodeCount: Int,
    val id: Int,
    val name: String,
    val overview: String,
    val posterPath: String,
    val seasonNumber: Int
) : Parcelable

@Deprecated("Not In Usages, will be removed")
@Parcelize
data class ProductionTVCompaniesModel(
    val id: Int,
    val logoPath: String?,
    val name: String,
    val originCountry: String
) : Parcelable

@Parcelize
data class ModelTvCreatedBy(

    @SerializedName("id")
    val id: Int,

    @SerializedName("credit_id")
    val creditId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("profile_path")
    val profilePath: String?

) : Parcelable
