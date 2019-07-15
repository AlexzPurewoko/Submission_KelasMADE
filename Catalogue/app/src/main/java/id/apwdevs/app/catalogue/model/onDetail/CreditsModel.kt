package id.apwdevs.app.catalogue.model.onDetail

import android.os.Parcelable
import id.apwdevs.app.catalogue.model.ResettableItem
import kotlinx.android.parcel.Parcelize

data class CreditsModel(
    val id: Int,
    val allCasts: ArrayList<CastModel>,
    val allCrew: ArrayList<CrewModel>
) : ResettableItem {
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
    val castId: Int?,
    val asCharacter: String,
    val creditId: String,
    val gender: Int?,
    val id: Int,
    var name: CharSequence,
    val order: Int,
    val profilePath: String?
) : ResettableItem, Parcelable {
    override fun onReset() {
        name = name.toString()
    }
}

@Parcelize
data class CrewModel(
    val job: String,
    val creditId: String,
    val gender: Int?,
    val id: Int,
    var name: CharSequence,
    val department: String,
    val profilePath: String?
) : ResettableItem, Parcelable {
    override fun onReset() {
        name = name.toString()
    }

}