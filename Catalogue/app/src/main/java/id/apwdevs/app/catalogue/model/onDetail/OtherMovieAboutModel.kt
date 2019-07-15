package id.apwdevs.app.catalogue.model.onDetail

import android.os.Parcelable
import id.apwdevs.app.catalogue.model.ResettableItem
import kotlinx.android.parcel.Parcelize

/**
 * Another details has been sended by other fragments in MainUserActivity,
 * so, we have to get another detail that represented in JSON
 */

@Parcelize
data class OtherMovieAboutModel(
    val movieBudget: Int,
    val homepage: String?,
    val productionCompanies: MutableList<ProductionCompaniesModel>,
    val productionCountry: MutableList<ProductionCountryModel>?,
    val revenue: Int,
    val runtime: Int?,
    val status: String,
    val tagLine: String?
) : ResettableItem, Parcelable {
    override fun onReset() {

    }

}

@Parcelize
data class ProductionCountryModel(
    val iso31661: String,
    val stringName: String
) : Parcelable

@Parcelize
data class ProductionCompaniesModel(
    val name: String,
    val id: Int,
    val logoPath: String?,
    val originCountry: String
) : Parcelable