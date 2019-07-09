package id.apwdevs.moTvCatalogue.model.onDetail

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
    val networks: MutableList<TvNetworkModel>,
    val productionCompanies: MutableList<ProductionTVCompaniesModel>,
    val productionTvSeasons: MutableList<ProductionTVSeasons>
)

class TvNetworkModel(
    val name: String,
    val id: Int,
    val logoPath: String,
    val originCountry: String
)

data class ProductionTVSeasons(
    val airDate: String,
    val episodeCount: Int,
    val id: Int,
    val name: String,
    val overview: String,
    val posterPath: String,
    val seasonNumber: Int
)

data class ProductionTVCompaniesModel(
    val id: Int,
    val logoPath: String?,
    val name: String,
    val originCountry: String
)

data class ModelTvCreatedBy(
    val id: Int,
    val creditId: String,
    val name: String,
    val gender: Int,
    val profilePath: String
)
