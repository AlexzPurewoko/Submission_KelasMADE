package id.apwdevs.app.catalogue.model.onDetail

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import id.apwdevs.app.catalogue.model.ClassResponse
import id.apwdevs.app.catalogue.model.ResettableItem
import kotlinx.android.parcel.Parcelize

data class CreditsModel(

    @SerializedName("id")
    val id: Int,

    @SerializedName("cast")
    val allCasts: List<CastModel>,

    @SerializedName("crew")
    val allCrew: List<CrewModel>
) : ResettableItem, ClassResponse {
    override fun onReset() {
        allCasts.forEach {
            it.onReset()
        }
        allCrew.forEach {
            it.onReset()
        }
    }
}

@Parcelize
data class CastModel(

    @SerializedName("cast_id")
    val castId: Int?,

    @SerializedName("character")
    val asCharacter: String,

    @SerializedName("credit_id")
    val creditId: String,

    @SerializedName("gender")
    val gender: Int?,

    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("order")
    val order: Int,

    @SerializedName("profile_path")
    val profilePath: String?

) : ResettableItem, Parcelable, ClassResponse {
    override fun onReset() {
    }
}

@Parcelize
data class CrewModel(

    @SerializedName("job")
    val job: String,

    @SerializedName("credit_id")
    val creditId: String,

    @SerializedName("gender")
    val gender: Int?,

    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("department")
    val department: String,

    @SerializedName("profile_path")
    val profilePath: String?

) : ResettableItem, Parcelable {
    override fun onReset() {

    }

}