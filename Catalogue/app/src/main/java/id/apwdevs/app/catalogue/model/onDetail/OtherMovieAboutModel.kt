package id.apwdevs.app.catalogue.model.onDetail

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import id.apwdevs.app.catalogue.model.ResettableItem
import kotlinx.android.parcel.Parcelize

/**
 * Another details has been sended by other fragments in MainUserActivity,
 * so, we have to get another detail that represented in JSON
 */

@Parcelize
data class OtherMovieAboutModel(

    @SerializedName("budget")
    val movieBudget: Int,

    @SerializedName("homepage")
    val homepage: String?,

    @SerializedName("revenue")
    val revenue: Int,

    @SerializedName("runtime")
    val runtime: Int?,

    @SerializedName("status")
    val status: String,

    @SerializedName("tagline")
    val tagLine: String?
) : ResettableItem, Parcelable {
    override fun onReset() {

    }

}

@Deprecated("Not in use. Will be removed!")
@Parcelize
data class ProductionCountryModel(
    val iso31661: String,
    val stringName: String
) : Parcelable

@Deprecated("Not in use. Will be removed!")
@Parcelize
data class ProductionCompaniesModel(
    val name: String,
    val id: Int,
    val logoPath: String?,
    val originCountry: String
) : Parcelable