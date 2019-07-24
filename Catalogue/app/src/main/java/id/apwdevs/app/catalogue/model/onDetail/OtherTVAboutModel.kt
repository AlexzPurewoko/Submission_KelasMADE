package id.apwdevs.app.catalogue.model.onDetail

import android.os.Parcelable
import id.apwdevs.app.catalogue.model.ResettableItem
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OtherTVAboutModel(
    val createdBy: MutableList<ModelTvCreatedBy>,
    val homepage: String,
    val firstAirDate: String,
    val inProduction: Boolean,
    val lastAirDate: String,
    val numberOfEpisodes: Int,
    val numberOfSeasons: Int,
    val originCountry: MutableList<String>,
    val status: String,
    val type: String,
    val productionCompanies: MutableList<ProductionTVCompaniesModel>,
    val productionTvSeasons: MutableList<ProductionTVSeasons>
) : Parcelable, ResettableItem {
    override fun onReset() {

    }

}

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

@Parcelize
data class ProductionTVCompaniesModel(
    val id: Int,
    val logoPath: String?,
    val name: String,
    val originCountry: String
) : Parcelable

@Parcelize
data class ModelTvCreatedBy(
    val id: Int,
    val creditId: String,
    val name: String,
    val profilePath: String?
) : Parcelable
