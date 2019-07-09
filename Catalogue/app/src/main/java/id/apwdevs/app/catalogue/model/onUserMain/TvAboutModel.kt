package id.apwdevs.app.catalogue.model.onUserMain

import android.os.Parcelable
import id.apwdevs.app.catalogue.model.GenreModel
import id.apwdevs.app.catalogue.model.ResettableItem
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TvAboutModel(
    val posterPath: String?,
    val popularity: Double,
    val idTv: Int,
    val backdropPath: String?,
    val voteAverage: Double,
    val overview: String?,
    var firstAirDate: CharSequence?,
    val originCountry: List<String>?,
    val genres: List<GenreModel>?,
    val originalLanguage: String?,
    val voteCount: Int,
    var name: CharSequence?,
    var originalName: CharSequence?
) : ResettableItem, Parcelable {
    override fun onReset() {
        name = name.toString()
        originalName = originalName.toString()
        firstAirDate = firstAirDate.toString()
        genres?.forEach {
            it.onReset()
        }
    }
}