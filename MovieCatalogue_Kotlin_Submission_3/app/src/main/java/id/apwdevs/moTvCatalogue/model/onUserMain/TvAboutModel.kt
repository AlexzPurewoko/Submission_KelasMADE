package id.apwdevs.moTvCatalogue.model.onUserMain

import android.os.Parcelable
import id.apwdevs.moTvCatalogue.model.GenreModel
import id.apwdevs.moTvCatalogue.model.ResettableItem
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TvAboutModel(
    val posterPath: String?,
    val popularity: Int,
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