package id.apwdevs.moTvCatalogue.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GenreModel(
    val id: Int,
    var genreName: CharSequence?
) : ResettableItem, Parcelable {
    override fun onReset() {
        genreName = genreName.toString()
    }
}
