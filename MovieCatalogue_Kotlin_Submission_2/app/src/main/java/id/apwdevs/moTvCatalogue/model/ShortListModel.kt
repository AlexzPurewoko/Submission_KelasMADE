package id.apwdevs.moTvCatalogue.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ShortListModel(
    val photoRes: Int,
    var overview: CharSequence?,
    var releaseDate: CharSequence?,
    var title: CharSequence?
) : Parcelable {
    fun resetSpannableString() {
        title = title.toString()
        releaseDate = releaseDate.toString()
        overview = overview.toString()
    }
}