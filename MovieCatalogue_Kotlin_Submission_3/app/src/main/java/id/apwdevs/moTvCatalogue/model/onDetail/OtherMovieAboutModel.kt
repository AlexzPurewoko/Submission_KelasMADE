package id.apwdevs.moTvCatalogue.model.onDetail

/**
 * Another details has been sended by other fragments in MainUserActivity,
 * so, we have to get another detail that represented in JSON
 */

data class OtherMovieAboutModel(
    val movieBudget: Int,
    val homepage: String?,
    val productionCompanies: MutableList<ProductionCompaniesModel>,
    val productionCountry: MutableList<ProductionCountryModel>?,
    val revenue: Int,
    val runtime: Int,
    val status: String,
    val tagLine: String?
)

data class ProductionCountryModel(
    val iso31661: String,
    val stringName: String
)

data class ProductionCompaniesModel(
    val name: String,
    val id: Int,
    val logoPath: String?,
    val originCountry: String
)